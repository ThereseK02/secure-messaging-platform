# SMP Attention Classifier

## Purpose

This component provides advisory machine-learning classification for received direct messages in the Secure Messaging Platform.

It estimates whether a message likely requires the receiver's attention or action.

## Current Model

The current model uses:

- Word-level TF-IDF features
- Character-level TF-IDF features
- Logistic Regression
- A default classification threshold of `0.50`

Model version:

```text
attention-combined-tfidf-logreg-v1
```

## Dataset

The current supervised dataset contains:

- 200 labeled messages
- 100 `NORMAL` messages
- 100 `NEEDS_ATTENTION` messages

Attention categories include:

- Deadline
- Follow-up
- Question
- Unresolved issue
- Urgent issue

The dataset contains contrast examples such as:

- Available versus unavailable
- Resolved versus unresolved
- Pending action versus completed action
- Active deadline versus completed deadline

## Evaluation

Five-fold stratified cross-validation produced:

```text
Accuracy:             0.730 ± 0.043
Attention precision:  0.705 ± 0.055
Attention recall:     0.810 ± 0.037
Attention F1:         0.751 ± 0.025
```

At the selected `0.50` threshold:

```text
True positives:   81
True negatives:   65
False positives:  35
False negatives:  19
```

The threshold favors recall because missing a message requiring attention may be more harmful than presenting an advisory false alert.

## Training

Activate the virtual environment:

```powershell
.\secure-messaging-ai\.venv\Scripts\Activate.ps1
```

Train and evaluate:

```powershell
python .\secure-messaging-ai\src\train_attention_model.py
```

Compare feature representations:

```powershell
python .\secure-messaging-ai\src\compare_attention_models.py
```

Evaluate thresholds:

```powershell
python .\secure-messaging-ai\src\evaluate_attention_thresholds.py
```

## Local Prediction

```powershell
python `
  .\secure-messaging-ai\src\predict_attention.py `
  "Please review the production logs before tomorrow."
```

Optional threshold:

```powershell
python `
  .\secure-messaging-ai\src\predict_attention.py `
  "Could you review the attachment?" `
  --threshold 0.60
```

## Security and Governance

The classifier is advisory only.

It does not:

- Mark messages as read
- Send or delete messages
- Block users
- Submit reports
- Change group membership
- Cast votes
- Resolve governance decisions
- Perform administrative actions

Only message text already authorized and decrypted by the authenticated SMP backend may be submitted for classification.

## Current Status

Implemented and locally tested:

- Dataset generation
- Model training
- Five-fold cross-validation
- Error analysis
- Feature-representation comparison
- Threshold analysis
- Local saved-model inference

Next:

- Expose inference through an internal authenticated service
- Connect Spring Boot to that service
- Add attention metadata to the authorized inbox response
- Replace the hard-coded frontend attention count

## Internal Inference API

The local FastAPI service loads the trained attention model once during application startup.

Available endpoints:

- `GET /health` reports service and model availability.
- `POST /classify` returns advisory attention metadata for authorized message text.

The classification endpoint requires the `X-Internal-API-Key` header.

The internal key is supplied through the `AI_INTERNAL_API_KEY` environment variable and is never stored in source control.

Start the service locally:

```powershell
$env:AI_INTERNAL_API_KEY = "local-development-key"

python -m uvicorn `
  attention_api:app `
  --app-dir .\secure-messaging-ai\src `
  --host 127.0.0.1 `
  --port 8090
```

Example authorized request:

```powershell
$headers = @{
  "X-Internal-API-Key" = "local-development-key"
}

$body = @{
  message = "Please investigate the failed deployment."
  threshold = 0.50
} | ConvertTo-Json

Invoke-RestMethod `
  -Method Post `
  -Uri "http://127.0.0.1:8090/classify" `
  -Headers $headers `
  -ContentType "application/json" `
  -Body $body
```

Local validation confirmed:

- Health endpoint availability
- Successful attention classification
- Successful normal-message classification
- Rejection of requests without the internal API key
- Graceful service startup and shutdown
