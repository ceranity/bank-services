from typing import Union
import json
import random
from fastapi import FastAPI, HTTPException

app = FastAPI()

def load_words_from_json(filename: str) -> Union[tuple[list[str], list[str]], HTTPException]:
    try:
        with open(filename, 'r') as f:
            words = json.load(f)
        if 'adjectives' not in words or 'nouns' not in words:
            raise ValueError("JSON file is missing required keys.")
        return words['adjectives'], words['nouns']
    except FileNotFoundError:
        raise HTTPException(status_code=404, detail="JSON file not found.")
    except json.JSONDecodeError:
        raise HTTPException(status_code=400, detail="Error decoding JSON file.")
    except ValueError as ve:
        raise HTTPException(status_code=400, detail=str(ve))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"An unexpected error occurred: {e}")

adjectives, nouns = load_words_from_json('words.json')

@app.get("/")
async def read_root():
    return {"message": "Welcome to the agent name generator! Head over to /generate to generate a name."}

@app.get("/agent-name/generate")
async def generate_name():
    if not adjectives or not nouns:
        raise HTTPException(status_code=500, detail="Adjectives or nouns are not loaded properly.")

    first_name = random.choice(adjectives).lower().capitalize()
    last_name = random.choice(nouns).lower().capitalize()
    return {"name": f"{first_name}-{last_name}"}
