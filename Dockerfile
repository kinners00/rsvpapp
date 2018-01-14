FROM teamcloudyuga/python:alpine
COPY . /usr/src/app
WORKDIR /usr/src/app
ENV LINK http://www.meetup.com/cloudyuga/
ENV TEXT1 CloudYuga
ENV TEXT2 Garage RSVP!
ENV LOGO https://upload.wikimedia.org/wikipedia/en/0/09/Puppet%27s_company_logo.png
ENV COMPANY Puppet Inc.
RUN pip3 install -r requirements.txt
CMD python rsvp.py