# jenkins-with-s3

Jenkins를 활용해 Spring Boot 애플리케이션의 자동 빌드, 테스트, 배포가 가능한 CI/CD 파이프라인을 구축하고, 결과물을 S3에 업로드하여 안정적인 배포를 자동화합니다.

##  ⚙️  기술 스택
VirtualBox: 가상 환경에서 여러 운영체제를 실행할 수 있게 해주는 오픈 소스 가상화 소프트웨어<br>
Docker: 컨테이너 기반 애플리케이션을 배포하고 관리할 수 있게 해주는 플랫폼<br>
Jenkins: 빌드 및 배포 자동화를 위한 CI 서버<br>
Ngrok: 로컬 Jenkins 서버에 외부 접근을 가능하게 하는 터널링 도구<br>
Spring Boot: 백엔드 애플리케이션 프레임워크<br>
Amazon EC2: 애플리케이션을 실행하는 클라우드 서버<br>
Amazon RDS : Spring Boot 애플리케이션의 데이터베이스를 제공하는 관리형 관계형 데이터베이스 서비스<br>
Amazon S3: 빌드된 JAR 파일을 저장하는 클라우드 스토리지<br>
AWS CLI: S3에 파일을 업로드하는 도구<br>

##  📜  파이프라인 흐름
1. GitHub → Jenkins 트리거
개발자가 코드를 GitHub에 푸시하면, Jenkins가 자동으로 빌드를 시작한다.<br>
Webhook을 설정해 GitHub에서 푸시나 PR(Pull Request)이 발생할 때 Jenkins가 이를 감지하여 빌드를 트리거한다.

2. Ngrok을 통한 로컬 Jenkins 공개
Jenkins가 로컬에서 실행 중일 경우, Ngrok을 사용하여 외부에서 GitHub Webhook 요청을 받을 수 있도록 Jenkins를 외부에 노출한다.

3. Spring Boot 애플리케이션 빌드
Jenkins는 GitHub에서 소스 코드를 가져와 Spring Boot 애플리케이션을 빌드한다.<br>
이 애플리케이션은 Amazon RDS와 연동되는 백엔드 서버이다.

4. Amazon S3에 JAR 파일 업로드
빌드가 완료되면, Jenkins는 AWS CLI를 사용해 빌드된 JAR 파일을 S3 버킷에 업로드한다.

5. EC2 인스턴스에서 애플리케이션 실행
EC2 인스턴스는 S3에서 업로드된 JAR 파일을 다운로드해 애플리케이션을 실행한다.<br>
이 과정을 통해 Spring Boot 애플리케이션이 자동으로 배포된다.

##  🚀  배포 흐름
1. GitHub에 코드 푸시<br>
2. Jenkins가 자동으로 빌드 실행<br>
3. 빌드된 JAR 파일을 Amazon S3에 업로드<br>
4. EC2 인스턴스에서 JAR 파일 다운로드 및 애플리케이션 실행<br>

## 🛠️ Docker 설치 및 설정
```bash
# 1. apt 인덱스 업데이트
$ sudo apt-get update

# 2. 필수 패키지 설치
$ sudo apt-get install ca-certificates curl gnupg lsb-release

# 3. Docker 공식 GPG 키 추가
$ sudo mkdir -m 0755 -p /etc/apt/keyrings
$ curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# 4. Docker 저장소를 APT 소스에 추가
$ echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 5. APT 패키지 캐시 업데이트
$ sudo apt-get update

# 6. Docker 설치
$ sudo apt-get install docker-ce docker-ce-cli [containerd.io](http://containerd.io/) docker-buildx-plugin docker-compose-plugin

# 7. Docker 명령어를 sudo 없이 사용하기 위한 사용자 권한 설정
$ sudo usermod -aG docker $USER # docker 명령어 사용시 sudo 권한 부여하는 설정(재부팅 필수)
$ newgrp docker    # 설정한 그룹 즉각 인식하는 명령어, 생략시 재부팅 후에만 group 적용

# 8. 설치 확인
$ docker --version

docker login -u [docker id]
```

## 🛠️ Netplan 설정
```bash
# Netplan 설정 파일 수정
$ sudo vi /etc/netplan/00-installer-config.yaml
```
![image1](https://github.com/user-attachments/assets/394bb572-2885-4ec4-8333-eab0c9c6c54f)
```bash
# 변경 사항 적용
$ sudo netplan apply
# 서버 재시작
$ sudo init 6
```
## 🛠️ 포트 번호 설정
### Jenkins : 8140, SSH : 8014
![image2](https://github.com/user-attachments/assets/281039d6-8a77-4c62-91ee-06040a19bf6b)
![image3](https://github.com/user-attachments/assets/66b58ce2-80a3-4b97-b786-2dd6a2347247)
![image4](https://github.com/user-attachments/assets/b5dca1ae-469a-4a81-aaa1-039e80146eba)
## 🛠️ Ngrok 설정
```bash
# 1. Ngrok 회원가입 후 인증 토큰 발급

# 2. Ngrok 토큰 등록
ngrok config add-authtoken [Your Ngrok Token]

# 3. Jenkins 서버를 외부에 노출
ngrok http 8140
```
![image5](https://github.com/user-attachments/assets/bb706bce-37a8-474e-a864-f4428ee7b752)
## 🛠️ Jenkins 컨테이너 실행
```bash
# Jenkins 컨테이너 생성 및 실행
$ docker run --name myjenkins --privileged -p 8140:8080 jenkins/jenkins:lts-jdk17
```
![image6](https://github.com/user-attachments/assets/bc394fe8-cb07-4fc7-bd0a-a9d7aedfe92e)
![image7](https://github.com/user-attachments/assets/c3404152-69f1-416a-94ea-c9db8ac97984)
```text
password : 
```
## 🛠️ Jenkins 프로젝트 구성
![image8](https://github.com/user-attachments/assets/fcba470f-968c-490b-84c7-783ba7a7c3ee)
![image9](https://github.com/user-attachments/assets/a21b83d0-deb3-49e8-a558-b6d8065f6ee8)
![image10](https://github.com/user-attachments/assets/53711370-9802-48ad-8e89-c3c3ea819fab)
![image11](https://github.com/user-attachments/assets/e09bdf83-0128-4dd7-9b11-1886b998b093)
## 🛠️ Jenkins Plugin 설치
![image12](https://github.com/user-attachments/assets/8e9310ea-866b-404b-84c7-029125d19091)
```bash
$ docker start myjenkins
```

### appjar 디렉터리 생성 후 권한 ERROR 발생
#### appjar 디렉터리를 별도로 생성하여 chown과 chmod으로 권한을 부여해 에러를 해결했지만, appjar 디렉터리에 jar를 넣으려다 말아서 필요없는 듯하다.
![image13](https://github.com/user-attachments/assets/17d63200-6445-4c17-94dd-22eefd8b755c)
#### X
![image14](https://github.com/user-attachments/assets/7fc0e330-ec2e-4142-b0e5-5113d8381712)
#### O
![image15](https://github.com/user-attachments/assets/ae394eff-a293-48f8-8cb8-d215a8858b5b)
![image16](https://github.com/user-attachments/assets/737afab5-a09a-4b06-a7e0-e4bde169741e)
```bash
pipeline {
    agent any

    stages {
        stage('Clone Repository') {
            steps {
                git branch: 'main', url: 'https://github.com/jjeong1015/CI-CD-Pipeline-with-Jenkins-and-AWS.git'
            }
        }
        
        stage('Compile and Build') {
            steps { # repository 폴더 구조 반영해야 하는 부분
                sh 'chmod +x gradlew'
                sh './gradlew clean build -x test'
                sh 'echo $WORKSPACE'
            }
        }
    }
}
```
![image17](https://github.com/user-attachments/assets/8f809ef4-197a-47b2-9456-ed6142b6a8a2)
```text
깃허브 토큰을 credentials Password에 입력
github_pat_
```
![image18](https://github.com/user-attachments/assets/163fd265-5208-422e-be95-b146e1f3ea1e)
![image19](https://github.com/user-attachments/assets/cc9d75fe-0bbb-42e8-975a-9b3e68088b0d)
### ERROR 발생
```text
[Pipeline] End of Pipeline
java.lang.UnsupportedOperationException: no known implementation of class org.jenkinsci.plugins.credentialsbinding.MultiBinding is named AmazonWebServicesCredentialsBinding
```
## 🛠️ Jenkins Plugin 설치
![image20](https://github.com/user-attachments/assets/7cffee52-b985-48b8-b072-d972636463e9)
```bash
$ docker start myjenkins
```
## 🛠️ AWS CLI 설치 및 구성
```bash
# Jenkins 컨테이너에 AWS CLI 설치
$ docker exec -u root -it myjenkins bash

$ apt install curl unzip
$ curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
$ unzip awscliv2.zip
$ ./aws/install
$ aws --version
$ aws configure
# AWS Access Key ID [None]: 
# AWS Secret Access Key [None]: 
# Default region name [None]: 
# Default output format [None]: 
```
### ERROR 발생
```text
+ aws s3 cp /var/jenkins_home/workspace/Mission14_01/build/libs/step18_empApp-0.0.1-SNAPSHOT.jar s3://ce26-bucket-02/
upload failed: build/libs/step18_empApp-0.0.1-SNAPSHOT.jar to s3://ce26-bucket-02/step18_empApp-0.0.1-SNAPSHOT.jar Unable to locate credentials
```
![image21](https://github.com/user-attachments/assets/f25ed749-35f5-4f91-a5f4-43149c626dcd)
![image22](https://github.com/user-attachments/assets/ec3bc7c5-7b58-4334-b83d-dd6fe497855e)
```bash
pipeline {
    agent any

    stages {
        stage('Clone Repository') {
            steps {
                // GitHub에서 코드 가져오기
                git branch: 'main', url: 'https://github.com/jjeong1015/CI-CD-Pipeline-with-Jenkins-and-AWS.git', credentialsId: 'jenkins-git-credential'
            }
        }

        stage('Compile and Build') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean build -x test'
            }
        }

        stage('Upload to S3') {
            steps {
                // AWS 자격 증명 사용하여 S3 업로드
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials']]) {
                    sh "aws s3 cp $WORKSPACE/build/libs/step18_empApp-0.0.1-SNAPSHOT.jar s3://ce26-bucket-02/"
                }
            }
        }
    }
}
```
![image23](https://github.com/user-attachments/assets/a131baf1-c454-4af9-8248-612f69b18c9a)
## 🛠️ Jenkins Plugin 설치
![image24](https://github.com/user-attachments/assets/4707dd46-7434-4eb8-b269-c622103cb88d)
```bash
$ docker start myjenkins
```

## 🛠️ SSH 키 설정
```bash
# SSH 키 생성
$ ssh-keygen -t rsa -b 4096 -C "이메일 주소"

# 기존 키 확인
$ ls ~/.ssh/
cat ~/.ssh/id_rsa
-----BEGIN OPENSSH PRIVATE KEY-----

-----END OPENSSH PRIVATE KEY-----

# Jenkins 컨테이너에 SSH 키 복사 및 권한 설정
$ docker cp /home/username/ce26-key.pem myjenkins:/var/jenkins_home/ce26-key.pem
$ docker exec -it myjenkins /bin/bash
$ chmod 400 /var/jenkins_home/ce26-key.pem
```
![image25](https://github.com/user-attachments/assets/cb0b9180-122b-4403-aa03-2a850066b37c)
![image26](https://github.com/user-attachments/assets/4b85a23c-a928-4ac9-9d82-5a1af4947d65)
```bash
## 🛠️ Jenkins 파이프라인 최종 설정
pipeline {
    agent any

    stages {
        stage('Clone Repository') {
            steps {
                // GitHub에서 코드 가져오기
                git branch: 'main', url: 'https://github.com/jjeong1015/CI-CD-Pipeline-with-Jenkins-and-AWS.git', credentialsId: 'jenkins-git-credential'
            }
        }

        stage('Compile and Build') {
            steps {
                sh 'chmod +x gradlew' // 빌드 스크립트에 실행 권한 부여
                sh './gradlew clean build -x test' // 테스트 제외하고 빌드
                sh 'echo $WORKSPACE' // 워크스페이스 경로 확인
            }
        }

        stage('Upload to S3') {
            steps {
                // AWS 자격 증명 사용하여 S3 업로드
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials']]) {
                    sh "aws s3 cp $WORKSPACE/build/libs/step18_empApp-0.0.1-SNAPSHOT.jar s3://ce26-bucket-02/"
                }
            }
        }
        
        stage('Connect to EC2') {
            steps {
                sshagent(['ssh-credentials-id']) {
                    // 1. JAR 파일을 EC2 인스턴스로 전송 (scp 명령 사용)
                    sh 'scp -i /var/jenkins_home/ce26-key.pem $WORKSPACE/build/libs/step18_empApp-0.0.1-SNAPSHOT.jar ubuntu@[public ip]:/home/ubuntu/'

                    // 2. EC2 인스턴스에서 JAR 파일을 백그라운드에서 실행 (ssh + nohup + & 사용)
                    sh 'ssh -i /var/jenkins_home/ce26-key.pem ubuntu@[public ip] "nohup sudo java -jar /home/ubuntu/step18_empApp-0.0.1-SNAPSHOT.jar > /dev/null 2>&1 &"'
                }
            }
        }
    }
}
```
![image27](https://github.com/user-attachments/assets/41aaa36d-6f30-4e4e-b8cd-d92920410909)
![image28](https://github.com/user-attachments/assets/c1292925-d545-40ee-8dcf-ab34d4698ef2)
![image29](https://github.com/user-attachments/assets/1e1a536e-1b5f-40bc-bbe3-97a709faea7e)
