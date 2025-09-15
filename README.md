# Kubernetes tutorijal - Beverage Service (Java 21 + Jersey + MongoDB + Nginx)
Tutorijal objašnjava šta je **Kubernetes** i zašto se koristi, njegove komponente i način njihove primene u konkretnom primeru. Fokus je na upotrebi Kubernetesa u okviru **Docker Desktop**-a. Isti stek je moguće pokrenuti i pomoću Minikube-a, ali se ovde namerno naglašava Docker Desktop radi jednostavnosti i činjenice da se Kubernetes-u isprva najčešće pristupa u kombinaciji sa Docker-om, te je ovaj tutorijal pogodan za početnike.

## Sadržaj
- [1) Šta je Kubernetes i zašto?](#1-šta-je-kubernetes-i-zašto)
- [2) Osnovne komponente](#2-osnovne-komponente)
- [3) Primena komponenti u ovom projektu](#3-primena-komponenti-u-ovom-projektu)
- [4) Struktura repozitorijuma](#4-struktura-repozitorijuma)
- [5) Preduslovi](#5-preduslovi)
- [6) Pokretanje nakon kloniranja](#6-pokretanje-nakon-kloniranja)
- [7) Kako kod funkcioniše](#7-kako-kod-funkcioniše)
- [8) Operacije nad stekom (start/stop/update)](#8-operacije-nad-stekom-startstopupdate)
- [9) Provera stanja i dijagnostika](#9-provera-stanja-i-dijagnostika)
- [10) Česta pitanja / Problemi](#10-česta-pitanja--problemi)
- [11) Napomena o Minikube-u](#11-napomena-o-minikube-u)

---

## 1) Šta je Kubernetes i zašto?
**Kubernetes (K8s)** je platforma za orkestraciju kontejnera, napravljena za upravljanje, skaliranje i upotrebu kontejnerizovanih aplikacija, odnosno pruža apstrakciju visokog nivoa za upravljanje aplikacijama. On raspoređuje i pokreće kontejnere na klasteru mašina, održava ih i obnovljava, skalira i obezbeđuje stabilnu mrežu i konfiguraciju.

**Zašto ga koristiti?**
- **Visoka dostupnost** – automatski restart problematičnih podova i raspodela replika.
- **Rolling update / rollback** – bezbedno uvođenje novih verzija i moguć brz povratak.
- **Deklarativna konfiguracija** – YAML fajlovi opisuju *željeno stanje*, K8s ga održava.
- **Otkrivanje servisa** – stabilni DNS/IP za međusobnu komunikaciju servisa.
- **Config/Secret** – bezbedno dodavanje konfiguracije i kredencijala.
- **Perzistencija** – PVC/Volume za stateful aplikacije (baze podataka).

---

## 2) Osnovne komponente 
- **Namespace** – izolacija grupe resursa u okrviru klastera (ovde `beverage`).
- **Pod** – najmanja jedinica izvršavanja (obično 1 kontejner).
- **Deployment** – upravlja stateless podovima (replike, rolling update).
- **StatefulSet** – za stateful servise (stabilni identiteti i PVC).
- **Service**
  - **ClusterIP** – interni IP/DNS vidljiv samo u klasteru.
  - **NodePort** – izlaganje porta na host-u (`localhost:30081`) za lokalni pristup.
- **ConfigMap** – konfiguracija koja nije poverljiva (npr. nginx.conf).
- **Secret** – poverljivi podaci (npr. korisničko ime/lozinka za bazu).
- **PersistentVolumeClaim (PVC)** – perzistentno skladište.
- **Probe** – `readinessProbe` (spremnost) i `livenessProbe` (živost).
- **Labels/Selectors** – povezivanje resursa (npr. `app=beverage-service`).

---

## 3) Primena komponenti u ovom projektu
Aplikacija je podeljena na tri dela:

- **Backend — `beverage-service`**
  - Java 21, **Jersey 3 (JAX-RS)** na **Grizzly**, **HK2** DI, **Jackson**, **Jakarta Validation**.
  - **Deployment** sa 2 replike + **Service (ClusterIP)**; health endpointi:
    - `GET /health` (readiness)  
    - `GET /live` (liveness)
  - Povezuje se na MongoDB preko promenljive okruženja `MONGO_URI`
    (npr. `mongodb://beverage:beveragepw@mongo-db:27017/beverage?authSource=beverage`).

- **Baza — `mongo-db`**
  - **MongoDB StatefulSet** (1 replika) sa **PVC** perzistencijom.
  - Kredencijali u **Secret**-u, interni pristup preko **Service (ClusterIP)**: `mongo-db`.

- **Frontend — `beverage-frontend`**
  - **Nginx** koji predstavlja proxy **`/api/*` → `beverage-service:8080`**.
  - Objavljen kao **Service (NodePort)** na **`http://localhost:30081`**.

**Arhitektura u kodu**: JAX-RS **Resources** → **Services** (poslovna logika) → **DAOs** (Data Access Objects - Mongo).  
DAO sloj: `BottleDaoMongo`, `CrateDaoMongo`, `BeverageDaoMongo`.

---

## 4) Struktura repozitorijuma
```
.
├─ src/                         # Java: resources, services, daos, db, dto, mappers, model
├─ build.gradle                 # Gradle (Groovy)
├─ settings.gradle
├─ Dockerfile                   # Multi-stage 
└─ k8s/
   ├─ config.yaml               # opciono: backend ConfigMap
   ├─ deployment.yaml           # backend Deployment (probe, env, replicas)
   ├─ frontend-config.yaml      # nginx.conf (proxy /api → backend)
   ├─ frontend-deployment.yaml
   ├─ frontend-service.yaml     # NodePort (30081)
   ├─ mongo-init.yaml           # opciono: inicijalni JS (kreiranje user/db)
   ├─ mongo-secret.yaml
   ├─ mongo-statefulset.yaml
   ├─ mongo-service.yaml
   ├─ namespace.yaml
   └─ service.yaml              # backend Service (ClusterIP)
```

---

## 5) Preduslovi
- **Docker Desktop** sa uključenim **Kubernetes** (Settings → Kubernetes → *Enable Kubernetes*).
- **kubectl** u PATH-u.
- **JDK 21**, **Gradle wrapper** (uključen).

---

## 6) Pokretanje nakon kloniranja

> Putanja je za **Docker Desktop Kubernetes** i lokalni Docker daemon.

**1) Kloniranje**
```powershell
git clone https://github.com/kristinajoks/kubernetes-tutorial
cd kubernetes-tutorial
```

**2) (Ako ima izmena u kodu) Build backend slike**  
Ukoliko kod nije menjan, ovaj korak se može preskočiti (Deployment već referencira tag).
```powershell
docker build -t beverage/service:2.0 .
```

**3) Primena svih yaml fajlova**
```powershell
kubectl apply -f k8s/
```

**4) Provera stanja**
```powershell
kubectl -n beverage rollout status statefulset/mongo-db
kubectl -n beverage rollout status deploy/beverage-service
kubectl -n beverage rollout status deploy/beverage-frontend
kubectl -n beverage get po,svc -o wide
```

**5) (Opciono) Ubacivanje MongoDB podataka**  
(komande prilagođene PowerShell-u)
```powershell
$DBPOD = kubectl -n beverage get po -l app=mongo-db -o jsonpath="{.items[0].metadata.name}"

kubectl -n beverage exec -it $DBPOD -- mongosh -u beverage -p beveragepw --authenticationDatabase beverage --quiet --eval 

test> db.getSiblingDB('beverage').bottles.insertMany([
  { id:101, name:'Cola 0.5L', volume:0.5, isAlcoholic:false, volumePercent:0, price:1.99, supplier:'Acme', inStock:120 },
  { id:102, name:'Beer 0.33L', volume:0.33, isAlcoholic:true, volumePercent:5.0, price:2.49, supplier:'BrewCo', inStock:42 }
])

kubectl -n beverage exec -it $DBPOD -- mongosh -u beverage -p beveragepw --authenticationDatabase beverage --quiet --eval 
test> db.getSiblingDB('beverage').crates.insertMany([
  { id:1, bottleId:101, bottlesPerCrate:20, price:34.50, inStock:10 },
  { id:2, bottleId:102, bottlesPerCrate:24, price:41.90, inStock:3 }
])

kubectl -n beverage exec -it $DBPOD -- mongosh -u beverage -p beveragepw --authenticationDatabase beverage --quiet --eval 
test> db.getSiblingDB('beverage').beverages.insertMany([
  { name:'Cola 0.5L', bottleId:101, crateId:1, volume:0.5, pricePerBottle:1.99, pricePerCrate:34.50,
    bottlesInStock:120, cratesInStock:10, totalBottlesInCrates:200, isAlcoholic:false, volumePercent:0.0 },
  { name:'Beer 0.33L', bottleId:102, crateId:2, volume:0.33, pricePerBottle:2.49, pricePerCrate:41.90,
    bottlesInStock:42,  cratesInStock:3,  totalBottlesInCrates:72,  isAlcoholic:true,  volumePercent:5.0 }
])
```

**6) Pristup aplikaciji (preko frontend NodePort-a)**
```
Frontend (welcome):            http://localhost:30081/
Backend health (preko proxy):  http://localhost:30081/api/health
API primeri:                   http://localhost:30081/api/bottles?page=1&perPage=1
                               http://localhost:30081/api/crates?page=1&perPage=1
                               http://localhost:30081/api/beverages?page=1&perPage=1
```

---

## 7) Kako kod funkcioniše
- **HealthResource** – `GET /health` i `GET /live` (K8s probe).
- **Resources** – JAX-RS REST sloj: `BottleResource`, `CrateResource`, `BeverageResource`, `HealthResource`.
- **Services** – poslovna logika, spajanje/filtriranje podataka.
- **DAOs** – isključivo Mongo pristup: `BottleDaoMongo`, `CrateDaoMongo`, `BeverageDaoMongo`.
- **MongoClientProvider** – HK2 Factory koji kreira Mongo klijenta na osnovu `MONGO_URI`.
- **Probes** – readiness (`/health`) obezbeđuje da saobraćaj krene tek kad je servis spreman; liveness (`/live`) omogućava auto-restart pri zaglavljivanju.

---

## 8) Operacije nad stekom (start/stop/update)

**Start (skaliranje naviše)**
```powershell
kubectl -n beverage scale statefulset mongo-db --replicas=1
kubectl -n beverage scale deploy beverage-service --replicas=2
kubectl -n beverage scale deploy beverage-frontend --replicas=1
```

**Stop (skaliranje naniže)**
```powershell
kubectl -n beverage scale deploy beverage-frontend --replicas=0
kubectl -n beverage scale deploy beverage-service --replicas=0
kubectl -n beverage scale statefulset mongo-db --replicas=0
```

**Restart bez promene image-a**
```powershell
kubectl -n beverage rollout restart deploy/beverage-service
kubectl -n beverage rollout restart deploy/beverage-frontend
kubectl -n beverage rollout restart statefulset/mongo-db
```

**Objava nove verzije backend-a**
```powershell
docker build -t beverage/service:2.1 .
kubectl -n beverage set image deploy/beverage-service app=beverage/service:2.1
kubectl -n beverage rollout status deploy/beverage-service
```

**Potpuni reset (obriši sve i primeni ponovo)**
```powershell
kubectl delete ns beverage --wait
kubectl apply -f k8s/
```

> U slučaju da se *ponovo koristi isti tag* slike, podesiti `imagePullPolicy: Always` u `deployment.yaml` ili uraditi `rollout restart` da bi podovi povukli novu verziju.

---

## 9) Provera stanja i dijagnostika
```powershell
# Pregled resursa
kubectl -n beverage get deploy,sts,po,svc -o wide

# Detalji i događaji
kubectl -n beverage describe deploy beverage-service
kubectl -n beverage describe sts mongo-db

# Logovi
kubectl -n beverage logs deploy/beverage-service --tail=200
kubectl -n beverage logs sts/mongo-db --tail=200

# Koja slika je trenutno u upotrebi?
kubectl -n beverage get deploy beverage-service -o jsonpath="{.spec.template.spec.containers[0].image}`n"

# Endpoints iza servisa
kubectl -n beverage get endpoints beverage-service
kubectl -n beverage get endpoints mongo-db

# Provera MONGO_URI u backend podu
kubectl -n beverage exec deploy/beverage-service -- printenv | findstr MONGO_URI
```

---

## 10) Česta pitanja / Problemi
- **Probe padaju (404) / CrashLoopBackOff**  
  Uveriti se da `HealthResource` postoji i da YAML sonde gađaju `/health` i `/live`.

- **Backend ne vidi Mongo (`UnknownHost` / timeout)**  
  Proveriti da `mongo-service.yaml` postoji i stvara servis `mongo-db` (ClusterIP), i da `MONGO_URI` cilja to ime.

- **Mongo “requires authentication”**  
  Proveriti da korisnik postoji i da URI sadrži user/pass i pravi `authSource`. Po potrebi napraviti korisnika kroz `mongosh` unutar Mongo poda.

- **NodePort ne radi**  
  Uveriti se da `frontend-service.yaml` zaista koristi `type: NodePort` i da port nije zauzet. Na Docker Desktop-u, pristup je `http://localhost:<nodePort>`.

- **Windows PowerShell saveti**  
  Za HTTP koristiti `curl.exe` (ne alias `curl`), a za duže `mongosh --eval` izraz nakon --eval izdvojiti u zasebnu komandu.

---

## 11) Napomena o Minikube-u
Isti yaml fajlovi mogu da rade i na **Minikube**-u (`minikube start` → `kubectl apply -f k8s/`). Pristup NodePort-u se tipično radi preko `minikube service`. Ovaj README ipak fokusira **Docker Desktop Kubernetes**.

---

Stek: **MongoDB (StatefulSet) + Java servis (Deployment) + Nginx (NodePort)**, sa health probes, rollout-ima i čistim DAO slojem za rad sa podacima.
