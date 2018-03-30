# OpenWhisk on Kubernetes Minikube

## Clone project

```
git clone git@github.com:apache/incubator-openwhisk-deploy-kube.git
cd incubator-openwhisk-deploy-kube/kubernetes
```

## Install required services on Minikube

### Setting Up Minikube for OpenWhisk

From: https://github.com/apache/incubator-openwhisk-deploy-kube/blob/master/docs/setting_up_minikube/README.md

First, download and install Minikube following these [instructions](https://github.com/kubernetes/minikube).

You will want at least 4GB of memory and 2 CPUs for Minikube to run OpenWhisk.
If you have a larger machine, you may want to provision more (especially more memory).

Start Minikube with:
```
minikube start --cpus 2 --memory 4096 --kubernetes-version=v1.8.0
```

Put the docker network in promiscuous mode.
```
minikube ssh -- sudo ip link set docker0 promisc on
```

Your Minikube cluster should now be ready to deploy OpenWhisk.

### Initial cluster setup

```
cd kubernetes/cluster-setup/
```

See: https://github.com/apache/incubator-openwhisk-deploy-kube/tree/master/kubernetes/cluster-setup#cluster-setup

### CouchDB

```
cd kubernetes/couchdb
```

See: https://github.com/apache/incubator-openwhisk-deploy-kube/tree/master/kubernetes/couchdb


## Install OpenWhisk components

### Deploy ApiGateway

```
cd kubernetes/apigateway
```

See: https://github.com/apache/incubator-openwhisk-deploy-kube/tree/master/kubernetes/apigateway

### Deploy Zookeeper

```
cd kubernetes/zookeeper
```

See: https://github.com/apache/incubator-openwhisk-deploy-kube/tree/master/kubernetes/zookeeper

### Deploy Kafka

```
cd kubernetes/kafka
```

See: https://github.com/apache/incubator-openwhisk-deploy-kube/tree/master/kubernetes/kafka

### Deploy Controller

```
cd kubernetes/controller
```

See: https://github.com/apache/incubator-openwhisk-deploy-kube/tree/master/kubernetes/controller

### Deploy Invoker

```
cd kubernetes/invoker
```

See: https://github.com/apache/incubator-openwhisk-deploy-kube/tree/master/kubernetes/invoker

### Deploy Nginx

```
cd kubernetes/nginx
```

See: https://github.com/apache/incubator-openwhisk-deploy-kube/tree/master/kubernetes/nginx

### Install RouteMgmt

```
cd kubernetes/routemgmt
```

See: https://github.com/apache/incubator-openwhisk-deploy-kube/tree/master/kubernetes/routemgmt

### Install Package Catalog

```
cd kubernetes/openwhisk-catalog
```

See: https://github.com/apache/incubator-openwhisk-deploy-kube/tree/master/kubernetes/openwhisk-catalog


## List of all commands in one place:

```
cd incubator-openwhisk-deploy-kube/kubernetes
cd cluster-setup/
kubectl apply -f namespace.yml
kubectl apply -f services.yml
kubectl -n openwhisk create cm whisk.config --from-env-file=config.env
kubectl -n openwhisk create cm whisk.runtimes --from-file=runtimes=runtimes.json
kubectl -n openwhisk create cm whisk.limits --from-env-file=limits.env
kubectl -n openwhisk create secret generic whisk.auth --from-file=system=auth.whisk.system --from-file=guest=auth.guest
cd ..
cd ingress/  # Lookup Minikube IP and svc/nginx NodePort-443 for below values 192.168.99.100:32521 (also CLI below)
kubectl -n openwhisk create configmap whisk.ingress --from-literal=api_host=192.168.99.100:32521 --from-literal=apigw_url=http://192.168.99.100:32521
cd ..
cd couchdb/
kubectl -n openwhisk create secret generic db.auth --from-literal=db_username=whisk_admin --from-literal=db_password=some_passw0rd
kubectl -n openwhisk create configmap db.config --from-literal=db_protocol=http --from-literal=db_provider=CouchDB --from-literal=db_whisk_activations=test_activations --from-literal=db_whisk_actions=test_whisks --from-literal=db_whisk_auths=test_subjects --from-literal=db_prefix=test_
kubectl apply -f couchdb.yml
cd ..
```

```
kubectl -n openwhisk logs -lname=couchdb
```
Wait for `successfully setup and configured CouchDB`

```
kubectl apply -f apigateway/apigateway.yml
kubectl apply -f zookeeper/zookeeper.yml
kubectl apply -f kafka/kafka.yml
cd controller/
kubectl -n openwhisk create cm controller.config --from-env-file=controller.env
kubectl apply -f controller.yml
cd ..
cd invoker/
kubectl label nodes --all openwhisk-role=invoker
kubectl -n openwhisk create cm invoker.config --from-env-file=invoker-dcf.env
kubectl apply -f invoker-dcf.yml
cd ..
cd nginx/
./certs.sh localhost
kubectl -n openwhisk create secret tls nginx --cert=certs/cert.pem --key=certs/key.pem
kubectl -n openwhisk create configmap nginx --from-file=nginx.conf
kubectl apply -f nginx.yml
cd ..
```

```
cd routemgmt/
kubectl apply -f install-routemgmt.yml
cd ..
cd openwhisk-catalog/
kubectl apply -f install-catalog.yml
cd ..
```

Install CLI from https://github.com/apache/incubator-openwhisk-cli/releases

Whisk CLI:
```
cd ingress/
wsk property set --auth `cat ../cluster-setup/auth.guest` --apihost 192.168.99.100:32521
cd ..
```


## What we have running

```
NAME                          TYPE                                  DATA      AGE
secrets/db.auth               Opaque                                2         43m
secrets/default-token-x72pj   kubernetes.io/service-account-token   3         44m
secrets/nginx                 kubernetes.io/tls                     2         37m
secrets/whisk.auth            Opaque                                2         44m

NAME                   DATA      AGE
cm/controller.config   2         39m
cm/db.config           6         43m
cm/invoker.config      9         38m
cm/nginx               1         37m
cm/whisk.config        5         44m
cm/whisk.ingress       2         43m
cm/whisk.limits        5         44m
cm/whisk.runtimes      1         44m

NAME             TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)                                     AGE
svc/apigateway   NodePort    10.105.87.38     <none>        8080:32420/TCP,9000:31909/TCP               44m
svc/controller   ClusterIP   10.97.3.201      <none>        8080/TCP                                    44m
svc/couchdb      ClusterIP   10.106.115.139   <none>        5984/TCP                                    42m
svc/kafka        ClusterIP   10.101.94.212    <none>        9092/TCP                                    44m
svc/nginx        NodePort    10.104.197.75    <none>        80:31576/TCP,443:32521/TCP,8443:32187/TCP   44m
svc/zookeeper    ClusterIP   10.98.215.98     <none>        2181/TCP,2888/TCP,3888/TCP                  44m

NAME                      DESIRED   CURRENT   AGE
statefulsets/controller   1         1         39m

NAME                DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
deploy/apigateway   1         1         1            1           40m
deploy/couchdb      1         1         1            1           42m
deploy/kafka        1         1         1            1           40m
deploy/nginx        1         1         1            1           37m
deploy/zookeeper    1         1         1            1           40m

NAME                             READY     STATUS    RESTARTS   AGE
po/apigateway-5ddf868478-8blms   2/2       Running   0          40m
po/controller-0                  1/1       Running   0          39m
po/couchdb-77b48d86d-b7vfs       1/1       Running   0          42m
po/invoker-dc777                 1/1       Running   0          38m
po/kafka-65cdc48b84-gzc6w        1/1       Running   0          40m
po/nginx-754d6d89b5-8cdjw        1/1       Running   0          37m
po/zookeeper-5485445cfd-m49wc    1/1       Running   0          40m

NAME                     DESIRED   SUCCESSFUL   AGE
jobs/install-catalog     1         1            36m
jobs/install-routemgmt   1         1            36m
```


## Issues

I changed cm/controller.config to have `java_opts=-Xmx1g`

It worked with Kubernetes v1.8.0

ERROR: Doesn't seem compatible with Kubernetes v1.9.4 - https://github.com/kubernetes/kubernetes/issues/61076
