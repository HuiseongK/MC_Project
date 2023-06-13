# MC_Project

## 앱 이름
WARU

## 앱 소개
기존의 다이어리 앱에서 감정분석을 통해 코멘트를 달아주는 기능을 추가하고 감정의 스코어를 기준으로 달력에 색을 통한 시각화를 제공하는 앱.

## 팀원
김규리 강희성 문장훈

## 사용한 라이브러리 및 API

Google Natural Language
https://cloud.google.com/natural-language?hl=ko

Kss-java
https://github.com/sangdee/kss-java

Material-calendarview
https://github.com/prolificinteractive/material-calendarview

okhttp3
https://square.github.io/okhttp/

openAI - gpt-3.5-turbo 모델 사용
https://openai.com/blog/openai-api


## 주의사항

resource/raw/ 
위치에 [openAI API KEY / Google Cloud API 서비스 계정 등록을 통해 발급받은 JSON]를 넣어야함. 

openAI API KEY를 공개된 repository에 올리게 되면 openAI에서 감지해 키를 사용할 수 없게 됨.

