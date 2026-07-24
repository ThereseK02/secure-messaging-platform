from pathlib import Path

import joblib
import numpy as np
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import classification_report, confusion_matrix
from sklearn.model_selection import (
    StratifiedKFold,
    cross_val_predict,
    cross_validate,
)
from sklearn.pipeline import FeatureUnion, Pipeline


PROJECT_DIR = Path(__file__).resolve().parents[1]
DATASET_PATH = PROJECT_DIR / "data" / "attention_messages.csv"
MODEL_PATH = PROJECT_DIR / "models" / "attention_model.joblib"
ERROR_REPORT_PATH = PROJECT_DIR / "data" / "attention_error_analysis.csv"


def load_dataset() -> pd.DataFrame:
    data = pd.read_csv(DATASET_PATH)

    required_columns = {"message", "label", "category"}
    missing_columns = required_columns.difference(data.columns)

    if missing_columns:
        raise ValueError(
            f"Dataset is missing required columns: {sorted(missing_columns)}"
        )

    data = data.dropna(subset=["message", "label"]).copy()
    data["message"] = data["message"].astype(str).str.strip()
    data["label"] = data["label"].astype(int)

    if data.empty:
        raise ValueError("Dataset contains no usable rows.")

    if data["label"].nunique() < 2:
        raise ValueError("Dataset must contain both label 0 and label 1.")

    if data["message"].duplicated().any():
        raise ValueError("Dataset contains duplicate messages.")

    return data


def build_pipeline() -> Pipeline:
    combined_features = FeatureUnion(
        transformer_list=[
            (
                "word",
                TfidfVectorizer(
                    lowercase=True,
                    analyzer="word",
                    ngram_range=(1, 2),
                    min_df=1,
                    max_features=3000,
                ),
            ),
            (
                "character",
                TfidfVectorizer(
                    lowercase=True,
                    analyzer="char_wb",
                    ngram_range=(3, 5),
                    min_df=1,
                    max_features=5000,
                ),
            ),
        ]
    )

    return Pipeline(
        steps=[
            (
                "features",
                combined_features,
            ),
            (
                "classifier",
                LogisticRegression(
                    random_state=42,
                    class_weight="balanced",
                    max_iter=1000,
                ),
            ),
        ]
    )


def print_metric_summary(
    metric_name: str,
    values: np.ndarray,
) -> None:
    print(
        f"{metric_name:<28}"
        f"{values.mean():.3f} ± {values.std():.3f}"
    )


def print_learned_features(
    model: Pipeline,
    limit: int = 15,
) -> None:
    features = model.named_steps["features"]
    classifier = model.named_steps["classifier"]

    word_vectorizer = features.transformer_list[0][1]
    character_vectorizer = features.transformer_list[1][1]

    word_feature_names = [
        f"word:{feature}"
        for feature in word_vectorizer.get_feature_names_out()
    ]

    character_feature_names = [
        f"char:{feature}"
        for feature in character_vectorizer.get_feature_names_out()
    ]

    feature_names = (
        word_feature_names
        + character_feature_names
    )

    coefficients = classifier.coef_[0]

    if len(feature_names) != len(coefficients):
        raise ValueError(
            "Feature-name count does not match coefficient count."
        )

    ranked_features = sorted(
        zip(feature_names, coefficients),
        key=lambda item: item[1],
    )

    print("\nFeatures associated with NORMAL:")
    for feature, weight in ranked_features[:limit]:
        print(f"  {feature:<40} {weight: .4f}")

    print("\nFeatures associated with NEEDS_ATTENTION:")
    for feature, weight in reversed(ranked_features[-limit:]):
        print(f"  {feature:<40} {weight: .4f}")


def main() -> None:
    data = load_dataset()

    messages = data["message"]
    labels = data["label"]

    print("Dataset rows:", len(data))

    print("\nLabel distribution:")
    print(labels.value_counts().sort_index())

    print("\nCategory distribution:")
    print(data["category"].value_counts().sort_index())

    cross_validator = StratifiedKFold(
        n_splits=5,
        shuffle=True,
        random_state=42,
    )

    scores = cross_validate(
        build_pipeline(),
        messages,
        labels,
        cv=cross_validator,
        scoring={
            "accuracy": "accuracy",
            "precision_attention": "precision",
            "recall_attention": "recall",
            "f1_attention": "f1",
        },
        return_train_score=False,
    )

    print("\nFive-fold stratified cross-validation:")
    print_metric_summary(
        "Accuracy",
        scores["test_accuracy"],
    )
    print_metric_summary(
        "Attention precision",
        scores["test_precision_attention"],
    )
    print_metric_summary(
        "Attention recall",
        scores["test_recall_attention"],
    )
    print_metric_summary(
        "Attention F1",
        scores["test_f1_attention"],
    )

    predictions = cross_val_predict(
        build_pipeline(),
        messages,
        labels,
        cv=cross_validator,
        method="predict",
    )

    probabilities = cross_val_predict(
        build_pipeline(),
        messages,
        labels,
        cv=cross_validator,
        method="predict_proba",
    )[:, 1]

    print("\nCross-validated confusion matrix:")
    print(
        confusion_matrix(
            labels,
            predictions,
        )
    )

    print("\nCross-validated classification report:")
    print(
        classification_report(
            labels,
            predictions,
            target_names=[
                "NORMAL",
                "NEEDS_ATTENTION",
            ],
            zero_division=0,
        )
    )

    analysis = data.copy()
    analysis["predicted_label"] = predictions
    analysis["attention_probability"] = probabilities
    analysis["correct"] = (
        analysis["label"]
        == analysis["predicted_label"]
    )

    analysis["error_type"] = "correct"

    analysis.loc[
        (analysis["label"] == 0)
        & (analysis["predicted_label"] == 1),
        "error_type",
    ] = "false_positive"

    analysis.loc[
        (analysis["label"] == 1)
        & (analysis["predicted_label"] == 0),
        "error_type",
    ] = "false_negative"

    error_report = analysis.loc[
        ~analysis["correct"]
    ].copy()

    error_report = error_report.sort_values(
        by=[
            "error_type",
            "attention_probability",
        ],
        ascending=[
            True,
            False,
        ],
    )

    error_report.to_csv(
        ERROR_REPORT_PATH,
        index=False,
        encoding="utf-8",
    )

    print("\nError counts:")
    print(error_report["error_type"].value_counts())

    print("\nFalse positives:")
    print(
        error_report.loc[
            error_report["error_type"] == "false_positive",
            [
                "message",
                "category",
                "attention_probability",
            ],
        ].to_string(index=False)
    )

    print("\nFalse negatives:")
    print(
        error_report.loc[
            error_report["error_type"] == "false_negative",
            [
                "message",
                "category",
                "attention_probability",
            ],
        ].to_string(index=False)
    )

    final_model = build_pipeline()
    final_model.fit(
        messages,
        labels,
    )

    print_learned_features(final_model)

    MODEL_PATH.parent.mkdir(
        parents=True,
        exist_ok=True,
    )

    joblib.dump(
        final_model,
        MODEL_PATH,
    )

    print("\nFinal model trained on all rows.")
    print(f"Model saved to: {MODEL_PATH}")
    print(f"Error report saved to: {ERROR_REPORT_PATH}")


if __name__ == "__main__":
    main()
