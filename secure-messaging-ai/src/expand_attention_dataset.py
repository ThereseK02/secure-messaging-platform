from pathlib import Path

import pandas as pd


PROJECT_DIR = Path(__file__).resolve().parents[1]
INPUT_PATH = PROJECT_DIR / "data" / "attention_messages_100_seed.csv"
OUTPUT_PATH = PROJECT_DIR / "data" / "attention_messages.csv"


NEW_NEEDS_ATTENTION = [
    ("The login service remains unavailable.", "urgent"),
    ("The login service is still unavailable after the restart.", "urgent"),
    ("The API continues to return errors.", "urgent"),
    ("The production outage has not been resolved.", "urgent"),
    ("Users still cannot access their accounts.", "urgent"),
    ("The server remains offline.", "urgent"),
    ("The database connection continues to fail.", "urgent"),
    ("Messages are still not being delivered.", "urgent"),
    ("The security incident is still active.", "urgent"),
    ("The payment service remains down.", "urgent"),

    ("The approval is still pending.", "unresolved"),
    ("The pull request has not yet been reviewed.", "unresolved"),
    ("No one has accepted responsibility for this task.", "unresolved"),
    ("The requested evidence is still missing.", "unresolved"),
    ("The incident remains unresolved.", "unresolved"),
    ("The deployment is still blocked.", "unresolved"),
    ("We are still waiting for the final decision.", "unresolved"),
    ("The access request has not been approved.", "unresolved"),
    ("The investigation has not been completed.", "unresolved"),
    ("The configuration problem remains open.", "unresolved"),

    ("The report is due tomorrow.", "deadline"),
    ("The certificate will expire in two days.", "deadline"),
    ("The application must be submitted tonight.", "deadline"),
    ("The security review must be completed this week.", "deadline"),
    ("The invoice requires approval before Friday.", "deadline"),
    ("The deployment must finish before the maintenance window.", "deadline"),
    ("The response is required by the end of the day.", "deadline"),
    ("The backup must be verified before midnight.", "deadline"),
    ("The registration deadline is approaching.", "deadline"),
    ("The final decision is required before Monday.", "deadline"),

    ("Are you available to review the change?", "question"),
    ("Has the incident been resolved?", "question"),
    ("Who is responsible for the deployment?", "question"),
    ("When will the report be completed?", "question"),
    ("Can you confirm the current server status?", "question"),
    ("Did the customer receive the response?", "question"),
    ("Why is the API still unavailable?", "question"),
    ("Should we postpone the release?", "question"),
    ("Have the permissions been verified?", "question"),
    ("Where is the missing attachment?", "question"),

    ("Please review the attached evidence.", "follow_up"),
    ("Please provide an update on the open incident.", "follow_up"),
    ("Please send the corrected configuration.", "follow_up"),
    ("Please verify the deployment status.", "follow_up"),
    ("Please complete the remaining security checks.", "follow_up"),
    ("Please contact the customer with an update.", "follow_up"),
    ("Please investigate the failed health check.", "follow_up"),
    ("Please approve or reject the pending request.", "follow_up"),
    ("Please upload the missing documentation.", "follow_up"),
    ("Please confirm when the service is restored.", "follow_up"),
]


NEW_NORMAL = [
    ("The login service is available again.", "normal"),
    ("The login issue was resolved after the restart.", "normal"),
    ("The API is responding normally.", "normal"),
    ("The production outage has been resolved.", "normal"),
    ("Users can access their accounts again.", "normal"),
    ("The server is back online.", "normal"),
    ("The database connection was restored.", "normal"),
    ("Messages are being delivered normally.", "normal"),
    ("The security incident has been closed.", "normal"),
    ("The payment service is operating normally.", "normal"),

    ("The approval was completed yesterday.", "normal"),
    ("The pull request has already been reviewed.", "normal"),
    ("The task owner has been assigned.", "normal"),
    ("The requested evidence was received.", "normal"),
    ("The incident is fully resolved.", "normal"),
    ("The deployment block was removed.", "normal"),
    ("The final decision has already been made.", "normal"),
    ("The access request was approved.", "normal"),
    ("The investigation was completed.", "normal"),
    ("The configuration problem was fixed.", "normal"),

    ("The report was submitted before the deadline.", "normal"),
    ("The certificate was renewed yesterday.", "normal"),
    ("The application was submitted successfully.", "normal"),
    ("The security review was completed early.", "normal"),
    ("The invoice was approved before Friday.", "normal"),
    ("The deployment finished before the maintenance window.", "normal"),
    ("The response was sent before the end of the day.", "normal"),
    ("The backup was verified before midnight.", "normal"),
    ("The registration deadline was extended.", "normal"),
    ("The final decision was recorded on Monday.", "normal"),

    ("I am available to review the change.", "normal"),
    ("The incident has been resolved.", "normal"),
    ("Therese is responsible for the deployment.", "normal"),
    ("The report was completed this morning.", "normal"),
    ("The current server status is healthy.", "normal"),
    ("The customer received the response.", "normal"),
    ("The API is no longer unavailable.", "normal"),
    ("The release will proceed as planned.", "normal"),
    ("The permissions have been verified.", "normal"),
    ("The attachment is included in the message.", "normal"),

    ("I reviewed the attached evidence.", "normal"),
    ("The open incident status was updated.", "normal"),
    ("The corrected configuration was sent.", "normal"),
    ("The deployment status was verified.", "normal"),
    ("The remaining security checks are complete.", "normal"),
    ("The customer received an update.", "normal"),
    ("The failed health check now passes.", "normal"),
    ("The pending request was approved.", "normal"),
    ("The missing documentation was uploaded.", "normal"),
    ("The service has been restored.", "normal"),
]


def build_new_rows() -> pd.DataFrame:
    rows = []

    for message, category in NEW_NEEDS_ATTENTION:
        rows.append(
            {
                "message": message,
                "label": 1,
                "category": category,
            }
        )

    for message, category in NEW_NORMAL:
        rows.append(
            {
                "message": message,
                "label": 0,
                "category": category,
            }
        )

    new_data = pd.DataFrame(rows)

    if len(new_data) != 100:
        raise ValueError(
            f"Expected 100 new rows, found {len(new_data)}."
        )

    return new_data


def main() -> None:
    original_data = pd.read_csv(INPUT_PATH)
    new_data = build_new_rows()

    combined_data = pd.concat(
        [
            original_data,
            new_data,
        ],
        ignore_index=True,
    )

    duplicate_mask = combined_data["message"].duplicated(
        keep=False
    )

    if duplicate_mask.any():
        duplicates = combined_data.loc[
            duplicate_mask,
            "message",
        ].tolist()

        raise ValueError(
            f"Duplicate messages found: {duplicates}"
        )

    if len(combined_data) != 200:
        raise ValueError(
            f"Expected 200 rows, found {len(combined_data)}."
        )

    label_counts = (
        combined_data["label"]
        .value_counts()
        .sort_index()
        .to_dict()
    )

    if label_counts != {0: 100, 1: 100}:
        raise ValueError(
            f"Expected 100 rows per label, found {label_counts}."
        )

    combined_data = combined_data.sample(
        frac=1,
        random_state=42,
    ).reset_index(drop=True)

    combined_data.to_csv(
        OUTPUT_PATH,
        index=False,
        encoding="utf-8",
    )

    print(f"Dataset written to: {OUTPUT_PATH}")
    print(f"Total rows: {len(combined_data)}")

    print("\nLabel distribution:")
    print(
        combined_data["label"]
        .value_counts()
        .sort_index()
    )

    print("\nCategory distribution:")
    print(
        combined_data["category"]
        .value_counts()
        .sort_index()
    )


if __name__ == "__main__":
    main()
