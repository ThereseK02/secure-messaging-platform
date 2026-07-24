from contextlib import asynccontextmanager
from pathlib import Path
import os

import joblib
from fastapi import Depends, FastAPI, Header, HTTPException, status
from pydantic import BaseModel, Field


PROJECT_DIR = Path(__file__).resolve().parents[1]
MODEL_PATH = PROJECT_DIR / "models" / "attention_model.joblib"

MODEL_VERSION = "attention-combined-tfidf-logreg-v1"
DEFAULT_THRESHOLD = 0.50

model = None


class ClassificationRequest(BaseModel):
    message: str = Field(
        min_length=1,
        max_length=10000,
        description="Authorized decrypted direct-message text.",
    )
    threshold: float = Field(
        default=DEFAULT_THRESHOLD,
        ge=0.0,
        le=1.0,
    )


class ClassificationResponse(BaseModel):
    needsAttention: bool
    attentionProbability: float
    threshold: float
    modelVersion: str


def verify_internal_api_key(
    x_internal_api_key: str | None = Header(default=None),
) -> None:
    expected_key = os.getenv("AI_INTERNAL_API_KEY")

    if not expected_key:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Internal API key is not configured.",
        )

    if x_internal_api_key != expected_key:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid internal API key.",
        )


def load_attention_model():
    if not MODEL_PATH.exists():
        raise RuntimeError(
            f"Attention model was not found: {MODEL_PATH}. "
            "Run train_attention_model.py first."
        )

    return joblib.load(MODEL_PATH)


@asynccontextmanager
async def lifespan(app: FastAPI):
    global model

    model = load_attention_model()

    yield

    model = None


app = FastAPI(
    title="SMP Attention Inference Service",
    version="1.0.0",
    description=(
        "Internal advisory message-attention classification service."
    ),
    lifespan=lifespan,
)


@app.get("/health")
def health() -> dict[str, object]:
    return {
        "status": "available",
        "modelLoaded": model is not None,
        "modelVersion": MODEL_VERSION,
    }


@app.post(
    "/classify",
    response_model=ClassificationResponse,
    dependencies=[Depends(verify_internal_api_key)],
)
def classify_message(
    request: ClassificationRequest,
) -> ClassificationResponse:
    if model is None:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Attention model is not loaded.",
        )

    normalized_message = request.message.strip()

    if not normalized_message:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Message must not be blank.",
        )

    probability = float(
        model.predict_proba(
            [normalized_message]
        )[0, 1]
    )

    return ClassificationResponse(
        needsAttention=probability >= request.threshold,
        attentionProbability=round(
            probability,
            6,
        ),
        threshold=request.threshold,
        modelVersion=MODEL_VERSION,
    )
