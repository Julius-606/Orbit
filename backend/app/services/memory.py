################################################################################
# FILE: backend/app/services/memory.py
# VERSION: 1.0.0 | SYSTEM: Orbit Memory Protocol
# IDENTITY: The Long-term Memory (Vector DB / ChromaDB)
################################################################################

import chromadb
from chromadb.utils import embedding_functions
import logging
import os

logger = logging.getLogger("Orbit-Memory")

class MemoryService:
    def __init__(self):
        # Store memory in a local directory
        persist_directory = os.path.join(os.getcwd(), "orbit_memory")
        self.client = chromadb.PersistentClient(path=persist_directory)

        # Using default embedding function (requires internet for some, but Chroma's default is local)
        self.collection = self.client.get_or_create_collection(
            name="orbit_user_preferences",
            metadata={"hnsw:space": "cosine"}
        )

    def learn(self, fact: str):
        """Orbit learns something about the user."""
        try:
            # We use the fact itself as the ID or a hash
            fact_id = str(hash(fact))
            self.collection.add(
                documents=[fact],
                ids=[fact_id]
            )
            logger.info(f"Orbit learned: {fact}")
        except Exception as e:
            logger.error(f"Failed to learn: {e}")

    def query(self, user_query: str, n_results: int = 3) -> str:
        """Orbit remembers relevant context."""
        try:
            results = self.collection.query(
                query_texts=[user_query],
                n_results=n_results
            )

            documents = results.get('documents', [[]])[0]
            if documents:
                return "\n".join(documents)
            return ""
        except Exception as e:
            logger.error(f"Memory retrieval failed: {e}")
            return ""

memory_service = MemoryService()
