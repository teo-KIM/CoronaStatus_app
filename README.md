# (2020/3/9 서비스 종료)

#### Google Play 정책에 따라 코로나 관련 민간 어플은 서비스 종료 되었습니다.

## 코로나 현황판 및 지도 App 

#### 해당 앱은 국내 코로나19 관련 현황을 현황판, 지도를 통해 한눈에 파악하고 실시간 알람을 수신할 수 있습니다.

## 프로젝트 정보

- #### 개발환경
  - 사용언어 : Kotlin, Php, Html, Javascript
  - 서버 환경 : Amazon ec2 (Amazon Linux AMI release 2018.03) ,Apache (2.4.41), PHP (7.0.33)
  - 데이터베이스 : Mysql(5.6.45)
  - 개발 OS : MacOS Catalina (10.15.1)

- #### 사용 라이브러리
  - **Back-End**
    +  cURL : FCM 전송
    +  Request, cheerio : 데이터 크롤링

  - **Front-End**
    + Firebase analytics : 앱 사용자 분석 
    + Okhttp3 : 서버와 http 통신
    + Play store core : 인 앱 업데이트
    + Firebase messaging : FCM 수신
    + libDaumMapAndroid : 카카오맵