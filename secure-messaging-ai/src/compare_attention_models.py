from pathlib import Path

import numpy as np
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import StratifiedKFold, cross_validate
from sklearn.pipeline import FeatureUnion, Pipeline


PROJECT_DIR = Path(__file__).resolve().parents[1]
DATASET_PATH = PROJECT_DIR / "data" / "attention_messages.csv"


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

    if data["message"].duplicated().any():
        raise ValueError("Dataset contains duplicate messages.")

    return data


def build_classifier() -> LogisticRegression:
    return LogisticRegression(
        random_state=42,
        class_weight="balanced",
        max_iter=1000,
    )


def build_word_model() -> Pipeline:
    return Pipeline(
        steps=[
            (
                "features",
                TfidfVectorizer(
                    lowercase=True,
                    analyzer="word",
                    ngram_range=(1, 2),
                    min_df=1,
                    max_features=3000,
                ),
            ),
            (
                "classifier",
                build_classifier(),
            ),
        ]
    )


def build_character_model() -> Pipeline:
    return Pipeline(
        steps=[
            (
                "features",
                TfidfVectorizer(
                    lowercase=True,
                    analyzer="char_wb",
                    ngram_range=(3, 5),
                    min_df=1,
                    max_features=5000,
                ),
            ),
            (
                "classifier",
                build_classifier(),
            ),
        ]
    )


def build_combined_model() -> Pipeline:
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
                build_classifier(),
            ),
        ]
    )


def evaluate_model(
    model_name: str,
    model: Pipeline,
    messages: pd.Series,
    labels: pd.Series,
    cross_validator: StratifiedKFold,
) -> dict[str, float | str]:
    scores = cross_validate(
        model,
        messages,
        labels,
        cv=cross_validator,
        scoring={
            "accuracy": "accuracy",
            "precision": "precision",
            "recall": "recall",
            "f1": "f1",
        },
        return_train_score=False,
    )

    return {
        "model": model_name,
        "accuracy_mean": scores["test_accuracy"].mean(),
        "accuracy_std": scores["test_accuracy"].std(),
        "precision_mean": scores["test_precision"].mean(),
        "recall_mean": scores["test_recall"].mean(),
        "f1_mean": scores["test_f1"].mean(),
        "f1_std": scores["test_f1"].std(),
    }


def main() -> None:
    data = load_dataset()

    messages = data["message"]
    labels = data["label"]

    cross_validator = StratifiedKFold(
        n_splits=5,
        shuffle=True,
        random_state=42,
    )

    models = [
        (
            "word_tfidf",
            build_word_model(),
        ),
        (
            "character_tfidf",
            build_character_model(),
        ),
        (
            "combined_word_character",
            build_combined_model(),
        ),
    ]

    results = []

    for model_name, model in models:
        print(f"Evaluating: {model_name}")

        result = evaluate_model(
            model_name,
            model,
            messages,
            labels,
            cross_validator,
        )

        results.append(result)

    results_table = pd.DataFrame(results)

    results_table = results_table.sort_values(
        by=[
            "f1_mean",
            "recall_mean",
        ],
        ascending=False,
    ).reset_index(drop=True)

    print("\nModel comparison:")
    print(
        results_table.to_string(
            index=False,
            formatters={
                "accuracy_mean": "{:.3f}".format,
                "accuracy_std": "{:.3f}".format,
                "precision_mean": "{:.3f}".format,
                "recall_mean": "{:.3f}".format,
                "f1_mean": "{:.3f}".format,
                "f1_std": "{:.3f}".format,
            },
        )
    )

    best_model = results_table.iloc[0]

    print("\nBest cross-validated model:")
    print(f"  Model: {best_model['model']}")
    print(f"  F1: {best_model['f1_mean']:.3f}")
    print(f"  Recall: {best_model['recall_mean']:.3f}")
    print(f"  Precision: {best_model['precision_mean']:.3f}")


if __name__ == "__main__":
    main()
