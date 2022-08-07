FROM ubuntu:latest
MAINTAINER Shazni Nazeer "shazni@gmail.com"

RUN apt-get update -y
RUN apt-get install -y python3-pip python3-dev build-essential

COPY . /app
WORKDIR /app
RUN pip3 install -r requirements.txt
WORKDIR /app/api

ENTRYPOINT ["python3"]
CMD ["api.py"]
