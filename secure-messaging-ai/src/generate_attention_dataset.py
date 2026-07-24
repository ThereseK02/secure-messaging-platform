from pathlib import Path

import pandas as pd


PROJECT_DIR = Path(__file__).resolve().parents[1]
OUTPUT_PATH = PROJECT_DIR / "data" / "attention_messages.csv"


NEEDS_ATTENTION = [
    ("Can you review the deployment logs before noon?", "follow_up"),
    ("The production server is down and users cannot log in.", "urgent"),
    ("Please respond by Friday with your decision.", "deadline"),
    ("Did you complete the database backup?", "question"),
    ("We still need approval before releasing this change.", "unresolved"),
    ("Call me immediately. This is an emergency.", "urgent"),
    ("Could you send the updated report today?", "follow_up"),
    ("The payment deadline expires tomorrow.", "deadline"),
    ("Why did the deployment fail again?", "question"),
    ("Please confirm that you received this message.", "follow_up"),
    ("Can you verify that the health endpoint is responding?", "question"),
    ("Please investigate the failed authentication requests.", "follow_up"),
    ("The customer needs an answer before the end of the day.", "deadline"),
    ("We have not resolved the database connection problem.", "unresolved"),
    ("The security certificate expires this week.", "deadline"),
    ("Can you approve the production release?", "question"),
    ("Please upload the revised document when it is ready.", "follow_up"),
    ("Users are reporting that messages are disappearing.", "urgent"),
    ("Who will complete the server migration?", "question"),
    ("We need a decision about the deployment strategy.", "unresolved"),
    ("Please check whether the backup completed successfully.", "follow_up"),
    ("The application is returning errors for every login attempt.", "urgent"),
    ("Can you join the incident call in ten minutes?", "urgent"),
    ("The final report must be submitted by Monday.", "deadline"),
    ("Could you explain why the test failed?", "question"),
    ("Please let me know when the issue is resolved.", "follow_up"),
    ("The release remains blocked until the security review is complete.", "unresolved"),
    ("We need your approval before purchasing the service.", "unresolved"),
    ("Can you update the project status this afternoon?", "follow_up"),
    ("The database storage is almost full.", "urgent"),
    ("Please confirm whether the meeting time still works.", "follow_up"),
    ("Why has the application not been deployed yet?", "question"),
    ("The support request has been waiting for three days.", "unresolved"),
    ("Please reset my account before tomorrow morning.", "deadline"),
    ("Can you send the missing attachment?", "question"),
    ("The production API is currently unavailable.", "urgent"),
    ("We still need someone to review the pull request.", "unresolved"),
    ("Please complete the access review by Thursday.", "deadline"),
    ("Could you verify the new user's permissions?", "follow_up"),
    ("The deployment cannot continue without the environment variable.", "unresolved"),
    ("Please respond as soon as possible.", "urgent"),
    ("Can you confirm who owns this task?", "question"),
    ("The invoice must be approved before it can be paid.", "deadline"),
    ("We have not received the requested evidence.", "unresolved"),
    ("Please investigate the unusual account activity.", "urgent"),
    ("Can you review the contract and provide feedback?", "follow_up"),
    ("The application needs to be restarted before the next test.", "deadline"),
    ("Who can help resolve the failed workflow?", "question"),
    ("Please acknowledge the final governance decision.", "follow_up"),
    ("The incident remains open and requires further investigation.", "unresolved"),
]


NORMAL = [
    ("Thank you for your help.", "normal"),
    ("The deployment completed successfully.", "normal"),
    ("I reviewed the document.", "normal"),
    ("That sounds good to me.", "normal"),
    ("The meeting notes are attached.", "normal"),
    ("Welcome to the group.", "normal"),
    ("I agree with the proposed approach.", "normal"),
    ("The application is running normally.", "normal"),
    ("Here is the information you requested.", "normal"),
    ("Have a good evening.", "normal"),
    ("The database backup completed successfully.", "normal"),
    ("I received your message.", "normal"),
    ("The production issue has been resolved.", "normal"),
    ("The report was submitted yesterday.", "normal"),
    ("Your access request was approved.", "normal"),
    ("The server restarted without errors.", "normal"),
    ("I have already reviewed the deployment logs.", "normal"),
    ("The meeting ended at three o'clock.", "normal"),
    ("The attachment contains the final version.", "normal"),
    ("Everything looks correct.", "normal"),
    ("The certificate was renewed successfully.", "normal"),
    ("The customer received the requested response.", "normal"),
    ("The security review is complete.", "normal"),
    ("The workflow finished successfully.", "normal"),
    ("The updated documentation is now available.", "normal"),
    ("I completed the database migration.", "normal"),
    ("The account was restored this morning.", "normal"),
    ("The team approved the release.", "normal"),
    ("The incident was closed after testing.", "normal"),
    ("The payment was processed yesterday.", "normal"),
    ("The urgent issue has already been fixed.", "normal"),
    ("The deadline was extended until next month.", "normal"),
    ("The question was answered during the meeting.", "normal"),
    ("The requested attachment was sent earlier.", "normal"),
    ("The failed test has now passed.", "normal"),
    ("The application was deployed successfully.", "normal"),
    ("The access review was completed on Thursday.", "normal"),
    ("The support request has been resolved.", "normal"),
    ("The production API is available again.", "normal"),
    ("The pull request was reviewed and merged.", "normal"),
    ("The final decision has been recorded.", "normal"),
    ("The project status was updated this afternoon.", "normal"),
    ("The invoice was approved and paid.", "normal"),
    ("The evidence was received and archived.", "normal"),
    ("The unusual activity was investigated and cleared.", "normal"),
    ("The contract review is complete.", "normal"),
    ("The environment variable was configured correctly.", "normal"),
    ("The user permissions were verified.", "normal"),
    ("The incident call concluded without further action.", "normal"),
    ("I hope you have a pleasant weekend.", "normal"),
]


def build_dataset() -> pd.DataFrame:
    rows = []

    for message, category in NEEDS_ATTENTION:
        rows.append(
            {
                "message": message,
                "label": 1,
                "category": category,
            }
        )

    for message, category in NORMAL:
        rows.append(
            {
                "message": message,
                "label": 0,
                "category": category,
            }
        )

    data = pd.DataFrame(rows)

    if len(data) != 100:
        raise ValueError(f"Expected 100 rows, but generated {len(data)}.")

    if data["message"].duplicated().any():
        duplicates = data.loc[
            data["message"].duplicated(),
            "message",
        ].tolist()

        raise ValueError(f"Duplicate messages found: {duplicates}")

    label_counts = data["label"].value_counts().to_dict()

    if label_counts != {1: 50, 0: 50}:
        raise ValueError(
            f"Expected 50 rows per label, but found {label_counts}."
        )

    return data


def main() -> None:
    data = build_dataset()

    data = data.sample(
        frac=1,
        random_state=42,
    ).reset_index(drop=True)

    OUTPUT_PATH.parent.mkdir(parents=True, exist_ok=True)

    data.to_csv(
        OUTPUT_PATH,
        index=False,
        encoding="utf-8",
    )

    print(f"Dataset written to: {OUTPUT_PATH}")
    print(f"Total rows: {len(data)}")

    print("\nLabel distribution:")
    print(data["label"].value_counts().sort_index())

    print("\nCategory distribution:")
    print(data["category"].value_counts().sort_index())


if __name__ == "__main__":
    main()
