# OpenWhisk/riff Java example for QR Codes

Based on: [IBM Cloud Blog: Building OpenWhisk actions with Java and Gradle](https://www.ibm.com/blogs/bluemix/2017/02/building-openwhisk-actions-java-gradle/)

## Build

```shell
./gradlew build
```

## Create OpenWhisk action/function

```shell
wsk -i action create qr build/libs/qr-codes-1.0.jar --main qr.Generate
```

## Invoke OpenWhisk action

```shell
wsk -i action invoke -br qr -p text 'Hello OpenWhisk!'
```

## Invoke OpenWhisk action to generate a QR PNG

```shell
wsk -i action invoke -br qr -p text 'Hello OpenWhisk!' | jq -r .qr | base64 -D > qr-wsk.png
```

## Create riff function

```shell
riff create java -n qr -a build/libs/qr-codes-1.0.jar --handler qr.Generate
```

## Invoke riff function

```shell
riff publish -i qr -d 'Hello riff!' -r
```

## Invoke riff function to generate a QR PNG

```shell
curl -H "Content-Type: text/plain" $(minikube -n riff-system service control-riff-http-gateway --url)/requests/qr -d 'Hello riff!' | base64 -D > qr-riff.png
```
