FROM teamcloudyuga/python:alpine
COPY . /usr/src/app
WORKDIR /usr/src/app
ENV LINK http://www.puppet.com
ENV TEXT1 Hello Demo Dojo!
ENV TEXT2 Demo RSVP!
ENV LOGO https://avatars3.githubusercontent.com/u/234268?s=400&v=4
ENV COMPANY Puppet Inc.
RUN pip3 install -r requirements.txt
CMD python rsvp.py