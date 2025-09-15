FROM python:3.11-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the requirements file and install dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy the source code into the container
COPY . ./app
COPY words.json .

# Expose the port that the app will run on
EXPOSE ${AGENT_NAME_PORT}

# Command to run the FastAPI app with Uvicorn, resolving the environment variable at runtime
CMD ["sh", "-c", "uvicorn app.main:app --host 0.0.0.0 --port $AGENT_NAME_PORT"]
