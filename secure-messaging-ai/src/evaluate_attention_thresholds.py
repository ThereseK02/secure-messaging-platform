from pathlib import Path

import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import (
    accuracy_score,
    confusion_matrix,
    f1_score,
    precision_score,
    recall_score,
)
from sklearn.model_selection import StratifiedKFold, cross_val_predict
from sklearn.pipeline import FeatureUnion, Pipeline


PROJECT_DIR = Path(__file__).resolve().parents[1]
DATASET_PATH = PROJECT_DIR / "data" / "attention_messages.csv"
OUTPUT_PATH = PROJECT_DIR / "data" / "attention_threshold_analysis.csv"


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
        raise ValueError("Dataset must contain both labels.")

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


def main() -> None:
    data = load_dataset()

    messages = data["message"]
    labels = data["label"]

    cross_validator = StratifiedKFold(
        n_splits=5,
        shuffle=True,
        random_state=42,
    )

    probabilities = cross_val_predict(
        build_pipeline(),
        messages,
        labels,
        cv=cross_validator,
        method="predict_proba",
    )[:, 1]

    thresholds = [
        0.40,
        0.45,
        0.50,
        0.55,
        0.60,
        0.65,
        0.70,
    ]

    rows = []

    for threshold in thresholds:
        predictions = (
            probabilities >= threshold
        ).astype(int)

        matrix = confusion_matrix(
            labels,
            predictions,
            labels=[0, 1],
        )

        true_negative = int(matrix[0, 0])
        false_positive = int(matrix[0, 1])
        false_negative = int(matrix[1, 0])
        true_positive = int(matrix[1, 1])

        rows.append(
            {
                "threshold": threshold,
                "accuracy": accuracy_score(
                    labels,
                    predictions,
                ),
                "precision": precision_score(
                    labels,
                    predictions,
                    zero_division=0,
                ),
                "recall": recall_score(
                    labels,
                    predictions,
                    zero_division=0,
                ),
                "f1": f1_score(
                    labels,
                    predictions,
                    zero_division=0,
                ),
                "true_negative": true_negative,
                "false_positive": false_positive,
                "false_negative": false_negative,
                "true_positive": true_positive,
                "flagged_total": int(predictions.sum()),
            }
        )

    results = pd.DataFrame(rows)

    results.to_csv(
        OUTPUT_PATH,
        index=False,
        encoding="utf-8",
    )

    print("Threshold comparison:")
    print(
        results.to_string(
            index=False,
            formatters={
                "threshold": "{:.2f}".format,
                "accuracy": "{:.3f}".format,
                "precision": "{:.3f}".format,
                "recall": "{:.3f}".format,
                "f1": "{:.3f}".format,
            },
        )
    )

    best_f1_row = results.loc[
        results["f1"].idxmax()
    ]

    print("\nHighest F1 threshold:")
    print(
        best_f1_row.to_string()
    )

    precision_candidates = results.loc[
        results["precision"] >= 0.75
    ]

    if not precision_candidates.empty:
        selected = precision_candidates.sort_values(
            by=[
                "recall",
                "f1",
            ],
            ascending=False,
        ).iloc[0]

        print("\nBest threshold with precision >= 0.75:")
        print(selected.to_string())
    else:
        print(
            "\nNo tested threshold achieved "
            "precision >= 0.75."
        )

    print(f"\nAnalysis saved to: {OUTPUT_PATH}")


if __name__ == "__main__":
    main()
