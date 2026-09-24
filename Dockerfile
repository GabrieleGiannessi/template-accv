FROM python:3.12-slim

WORKDIR /app

# Install dependencies
COPY pyproject.toml README.md /app/
COPY src/ /app/src/
RUN pip install --no-cache-dir .

# Copy assets, data, and web UI
COPY assets/ /app/assets/
COPY data/ /app/data/
COPY web/ /app/web/
COPY main.py generate_figurine.py /app/

EXPOSE 8000
ENV PORT=8000

CMD ["python", "main.py", "serve", "--host", "0.0.0.0", "--port", "8000"]
