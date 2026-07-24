from pathlib import Path
import argparse
import json
import sys

import joblib


PROJECT_DIR = Path(__file__).resolve().parents[1]
MODEL_PATH = PROJECT_DIR / "models" / "attention_model.joblib"

DEFAULT_THRESHOLD = 0.50
MODEL_VERSION = "attention-combined-tfidf-logreg-v1"


def load_model():
    if not MODEL_PATH.exists():
        raise FileNotFoundError(
            f"Model file was not found: {MODEL_PATH}. "
            "Run train_attention_model.py first."
        )

    return joblib.load(MODEL_PATH)


def classify_message(
    model,
    message: str,
    threshold: float,
) -> dict[str, object]:
    normalized_message = message.strip()

    if not normalized_message:
        raise ValueError("Message must not be empty.")

    probability = float(
        model.predict_proba(
            [normalized_message]
        )[0, 1]
    )

    needs_attention = probability >= threshold

    return {
        "message": normalized_message,
        "needsAttention": needs_attention,
        "attentionProbability": round(
            probability,
            6,
        ),
        "threshold": threshold,
        "modelVersion": MODEL_VERSION,
    }


def parse_arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description=(
            "Classify whether a direct message "
            "likely needs the receiver's attention."
        )
    )

    parser.add_argument(
        "message",
        nargs="?",
        help="Message text to classify.",
    )

    parser.add_argument(
        "--threshold",
        type=float,
        default=DEFAULT_THRESHOLD,
        help=(
            "Classification threshold between 0 and 1. "
            f"Default: {DEFAULT_THRESHOLD}"
        ),
    )

    return parser.parse_args()


def main() -> None:
    arguments = parse_arguments()

    if not 0.0 <= arguments.threshold <= 1.0:
        raise ValueError(
            "Threshold must be between 0 and 1."
        )

    message = arguments.message

    if message is None:
        message = input(
            "Enter a message to classify: "
        )

    model = load_model()

    result = classify_message(
        model,
        message,
        arguments.threshold,
    )

    print(
        json.dumps(
            result,
            indent=2,
        )
    )


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        print(
            json.dumps(
                {
                    "error": str(error),
                },
                indent=2,
            ),
            file=sys.stderr,
        )
        sys.exit(1)
