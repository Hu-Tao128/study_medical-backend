# MongoDB schema (content-first)

## Principle
- MongoDB stores independent content documents for fast reads.
- PostgreSQL keeps relationships and transactional progress.
- IDs like `user_id`, `topic_id`, and `group_id` are logical references resolved in backend use cases.

## Collections

### `notes`
- Fields: `userId`, `title`, `contentMd`, `topicId`, `tags`, `aiSummary`, `aiEmbeddingsId`, `isFavorite`, `isArchived`, `createdAt`, `updatedAt`
- Indexes:
  - `{ userId: 1 }`
  - `{ topicId: 1 }`
  - `{ userId: 1, updatedAt: -1 }`

### `chat_buckets`
- Fields: `roomId`, `bucketIndex`, `count`, `messages[]`, `createdAt`, `updatedAt`
- Bucket rule: max 50 messages per document.
- Indexes:
  - `{ roomId: 1, bucketIndex: -1 }`

### `flashcards`
- Fields: `topicId`, `createdBy`, `question`, `answer`, `tags`, `difficulty`, `visibility`, `groupId`, `aiGenerated`, `aiModel`, `aiSource`, `aiEmbeddingsId`, `createdAt`
- Indexes:
  - `{ topicId: 1 }`
  - `{ createdBy: 1 }`

### `quizzes`
- Fields: `title`, `topicId`, `createdBy`, `questions[]`, `visibility`, `groupId`, `aiGenerated`, `aiModel`, `aiSource`, `aiEmbeddingsId`, `createdAt`
- Embedded question fields: `question`, `options`, `correctAnswer`, `explanation`, `aiGenerated`

### `clinical_cases`
- Fields: `title`, `topicId`, `description`, `symptoms[]`, `diagnosis`, `questions[]`, `assets[]`, `difficulty`, `visibility`, `groupId`, `aiGenerated`, `aiModel`, `aiSource`, `aiEmbeddingsId`, `createdAt`

## AI metadata rule
- AI is metadata, not a separate aggregate root:
  - `aiGenerated`
  - `aiModel`
  - `aiSource`
  - `aiEmbeddingsId`

## Ownership boundary
- MongoDB: `notes`, `chat_buckets`, `flashcards`, `quizzes`, `clinical_cases`
- PostgreSQL: `users`, `groups`, `memberships`, `topics`, `user_progress`, `study_sessions`, `embeddings`
