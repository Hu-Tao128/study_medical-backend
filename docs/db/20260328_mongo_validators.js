// Run with mongosh against the application Mongo database.
// This script is idempotent: it creates collections if missing and updates
// validators/indexes if they already exist.

function ensureCollectionWithValidator(name, validator) {
  const exists = db.getCollectionNames().includes(name);

  if (!exists) {
    db.createCollection(name, {
      validator,
      validationLevel: "strict",
      validationAction: "error",
    });
    return;
  }

  db.runCommand({
    collMod: name,
    validator,
    validationLevel: "strict",
    validationAction: "error",
  });
}

// notes
ensureCollectionWithValidator("notes", {
  $jsonSchema: {
    bsonType: "object",
    required: ["userId", "title", "contentMd", "createdAt", "updatedAt"],
    properties: {
      userId: { bsonType: ["binData", "string"] },
      title: { bsonType: "string", minLength: 1, maxLength: 200 },
      contentMd: { bsonType: "string" },
      topicId: { bsonType: ["binData", "string", "null"] },
      tags: { bsonType: "array", items: { bsonType: "string" } },
      aiSummary: { bsonType: ["string", "null"] },
      aiEmbeddingsId: { bsonType: ["string", "null"] },
      aiGenerated: { bsonType: "bool" },
      aiModel: { bsonType: ["string", "null"] },
      aiSource: { bsonType: ["string", "null"] },
      isFavorite: { bsonType: "bool" },
      isArchived: { bsonType: "bool" },
      createdAt: { bsonType: "date" },
      updatedAt: { bsonType: "date" },
    },
  },
});

db.notes.createIndex({ userId: 1 }, { name: "idx_notes_user" });
db.notes.createIndex({ topicId: 1 }, { name: "idx_notes_topic" });
db.notes.createIndex({ userId: 1, updatedAt: -1 }, { name: "idx_notes_user_updated" });

// chat_buckets
ensureCollectionWithValidator("chat_buckets", {
  $jsonSchema: {
    bsonType: "object",
    required: ["roomId", "bucketIndex", "messages", "count", "createdAt", "updatedAt"],
    properties: {
      roomId: { bsonType: ["binData", "string"] },
      bucketIndex: { bsonType: "int" },
      count: { bsonType: "int" },
      messages: {
        bsonType: "array",
        items: {
          bsonType: "object",
          required: ["senderId", "text", "type", "createdAt"],
          properties: {
            senderId: { bsonType: ["binData", "string"] },
            text: { bsonType: "string" },
            type: { enum: ["TEXT", "IMAGE", "FILE"] },
            createdAt: { bsonType: "date" },
          },
        },
      },
      createdAt: { bsonType: "date" },
      updatedAt: { bsonType: "date" },
    },
  },
});

db.chat_buckets.createIndex({ roomId: 1, bucketIndex: -1 }, { name: "idx_chat_room_bucket" });

// flashcards
ensureCollectionWithValidator("flashcards", {
  $jsonSchema: {
    bsonType: "object",
    required: ["topicId", "createdBy", "question", "answer", "difficulty", "visibility", "createdAt"],
    properties: {
      topicId: { bsonType: ["binData", "string"] },
      createdBy: { bsonType: ["binData", "string"] },
      question: { bsonType: "string" },
      answer: { bsonType: "string" },
      tags: { bsonType: "array", items: { bsonType: "string" } },
      difficulty: { enum: ["EASY", "MEDIUM", "HARD"] },
      visibility: { enum: ["PRIVATE", "GROUP", "PUBLIC"] },
      groupId: { bsonType: ["binData", "string", "null"] },
      aiGenerated: { bsonType: "bool" },
      aiModel: { bsonType: ["string", "null"] },
      aiSource: { bsonType: ["string", "null"] },
      aiEmbeddingsId: { bsonType: ["string", "null"] },
      createdAt: { bsonType: "date" },
    },
  },
});

db.flashcards.createIndex({ topicId: 1 }, { name: "idx_flashcards_topic" });
db.flashcards.createIndex({ createdBy: 1 }, { name: "idx_flashcards_created_by" });

// quizzes
ensureCollectionWithValidator("quizzes", {
  $jsonSchema: {
    bsonType: "object",
    required: ["title", "topicId", "createdBy", "questions", "visibility", "createdAt"],
    properties: {
      title: { bsonType: "string" },
      topicId: { bsonType: ["binData", "string"] },
      createdBy: { bsonType: ["binData", "string"] },
      visibility: { enum: ["PRIVATE", "GROUP", "PUBLIC"] },
      groupId: { bsonType: ["binData", "string", "null"] },
      aiGenerated: { bsonType: "bool" },
      aiModel: { bsonType: ["string", "null"] },
      aiSource: { bsonType: ["string", "null"] },
      aiEmbeddingsId: { bsonType: ["string", "null"] },
      createdAt: { bsonType: "date" },
      questions: {
        bsonType: "array",
        minItems: 1,
        items: {
          bsonType: "object",
          required: ["question", "options", "correctAnswer"],
          properties: {
            question: { bsonType: "string" },
            options: { bsonType: "array", minItems: 2, items: { bsonType: "string" } },
            correctAnswer: { bsonType: "int" },
            explanation: { bsonType: ["string", "null"] },
            aiGenerated: { bsonType: "bool" },
          },
        },
      },
    },
  },
});

db.quizzes.createIndex({ topicId: 1 }, { name: "idx_quizzes_topic" });
db.quizzes.createIndex({ createdBy: 1 }, { name: "idx_quizzes_created_by" });

// clinical_cases
ensureCollectionWithValidator("clinical_cases", {
  $jsonSchema: {
    bsonType: "object",
    required: ["title", "description", "topicId", "createdBy", "difficulty", "visibility", "createdAt"],
    properties: {
      title: { bsonType: "string" },
      topicId: { bsonType: ["binData", "string"] },
      createdBy: { bsonType: ["binData", "string"] },
      description: { bsonType: "string" },
      symptoms: { bsonType: "array", items: { bsonType: "string" } },
      diagnosis: { bsonType: ["string", "null"] },
      difficulty: { enum: ["EASY", "MEDIUM", "HARD"] },
      visibility: { enum: ["PRIVATE", "GROUP", "PUBLIC"] },
      groupId: { bsonType: ["binData", "string", "null"] },
      aiGenerated: { bsonType: "bool" },
      aiModel: { bsonType: ["string", "null"] },
      aiSource: { bsonType: ["string", "null"] },
      aiEmbeddingsId: { bsonType: ["string", "null"] },
      createdAt: { bsonType: "date" },
      questions: {
        bsonType: "array",
        items: {
          bsonType: "object",
          required: ["question", "options", "correctAnswer"],
          properties: {
            question: { bsonType: "string" },
            options: { bsonType: "array", minItems: 2, items: { bsonType: "string" } },
            correctAnswer: { bsonType: "int" },
            explanation: { bsonType: ["string", "null"] },
          },
        },
      },
      assets: {
        bsonType: "array",
        items: {
          bsonType: "object",
          required: ["type", "url"],
          properties: {
            type: { bsonType: "string" },
            url: { bsonType: "string" },
          },
        },
      },
    },
  },
});

db.clinical_cases.createIndex({ topicId: 1 }, { name: "idx_cases_topic" });
db.clinical_cases.createIndex({ createdBy: 1 }, { name: "idx_cases_created_by" });
