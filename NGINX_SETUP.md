# 🔄 Nginx Proxy Setup - Question Master API

## 📋 Visão Geral

Este documento descreve a configuração do Nginx como proxy reverso para a API Question Master.

### Arquitetura

```
Internet → Nginx (80) → Spring Boot API (8080) → Supabase PostgreSQL
```

---

## 🚀 Configuração

### Arquivos Criados

1. **`nginx.conf`** - Configuração principal do Nginx
2. **`docker-compose.yml`** - Atualizado com serviço Nginx
3. **`application-docker.properties`** - Configurado para usar forward headers

---

## 📦 Estrutura de Serviços

### Docker Compose

```yaml
services:
  api:          # Spring Boot API na porta 8080 (exposed internamente)
                # Conecta ao Supabase PostgreSQL
  nginx:        # Proxy reverso nas portas 80/443
```

---

## 🔧 Configuração do Nginx

### Características Implementadas

✅ **Proxy Reverso** - Encaminha requisições para a API  
✅ **Rate Limiting** - 100 req/s com burst de 20  
✅ **Gzip Compression** - Compressão de respostas  
✅ **Security Headers** - Headers de segurança  
✅ **Logging** - Logs de acesso e erros  
✅ **Health Checks** - Endpoint de saúde  
✅ **Static Assets** - Cache de arquivos estáticos  

### Endpoints Expostos

| Endpoint | Descrição |
|----------|-----------|
| `/` | Todos os endpoints da API |
| `/health` | Health check da aplicação |
| `/*.*` | Assets estáticos com cache |

---

## 🚀 Como Usar

### 1. Subir a Stack Completa

```bash
# Criar diretório para logs
mkdir -p logs/nginx

# Build e start
docker-compose up --build -d

# Ver logs
docker-compose logs -f nginx
docker-compose logs -f api
```

### 2. Testar a API

```bash
# Via Nginx (porta 80)
curl http://localhost/api/exams

# Via API direta (porta 8080)
curl http://localhost:8080/api/exams

# Health check
curl http://localhost/health

# Swagger UI
open http://localhost/swagger-ui.html
```

### 3. Verificar Logs

```bash
# Logs do Nginx
tail -f logs/nginx/access.log
tail -f logs/nginx/error.log

# Via Docker
docker-compose logs -f nginx
docker exec questionmaster-nginx tail -f /var/log/nginx/access.log
```

---

## 🔐 Rate Limiting

Configurado: **100 requests/segundo** com burst de 20

Se exceder:
```json
{
  "error": "Too Many Requests",
  "status": 429
}
```

Para alterar, edite `nginx.conf`:
```nginx
limit_req_zone $binary_remote_addr zone=api_limit:10m rate=100r/s;
```

---

## 📊 Monitoramento

### Logs de Acesso

Formato padrão do Nginx:
```
192.168.1.1 - - [15/Jan/2024:10:30:00 +0000] "GET /api/exams HTTP/1.1" 200 1234 "-"
```

### Métricas Úteis

```bash
# Requisições por IP
awk '{print $1}' logs/nginx/access.log | sort | uniq -c | sort -rn

# Top endpoints
awk '{print $7}' logs/nginx/access.log | sort | uniq -c | sort -rn

# Status codes
awk '{print $9}' logs/nginx/access.log | sort | uniq -c | sort -rn

# Rate limiting hits
grep "503" logs/nginx/error.log | wc -l
```

---

## 🔄 Configuração de Produção

### SSL/HTTPS (Opcional)

Para habilitar HTTPS:

1. **Gerar certificados**:
```bash
# Let's Encrypt
certbot certonly --standalone -d api.example.com
```

2. **Atualizar nginx.conf**:
```nginx
server {
    listen 443 ssl http2;
    ssl_certificate /etc/nginx/ssl/fullchain.pem;
    ssl_certificate_key /etc/nginx/ssl/privkey.pem;
    
    # ... resto da configuração
}

# Redirect HTTP to HTTPS
server {
    listen 80;
    return 301 https://$host$request_uri;
}
```

3. **Atualizar docker-compose.yml**:
```yaml
nginx:
  volumes:
    - ./ssl:/etc/nginx/ssl:ro
  ports:
    - "443:443"
```

### Variáveis de Ambiente

```bash
# .env
NGINX_WORKER_PROCESSES=auto
NGINX_MAX_WORKER_CONNECTIONS=1024
NGINX_RATE_LIMIT=100r/s
```

---

## 🐛 Troubleshooting

### Problema: 502 Bad Gateway

**Causa:** API não está respondendo

**Solução:**
```bash
# Verificar se a API está rodando
docker-compose ps

# Ver logs da API
docker-compose logs api

# Reiniciar API
docker-compose restart api
```

### Problema: 503 Service Unavailable

**Causa:** Rate limiting atingido

**Solução:**
```bash
# Ver logs do Nginx
docker logs questionmaster-nginx

# Ajustar rate limit em nginx.conf
# Ou adicionar whitelist de IPs
```

### Problema: CORS Errors

**Causa:** Headers CORS não estão configurados

**Solução:**
O Spring Boot já configura CORS. Verifique:
```bash
# Ver configuração em SecurityConfig.kt
# Adicionar origem se necessário
```

---

## 📈 Performance

### Benchmarks

**Com Nginx:**
- ✅ Menor latência (keepalive)
- ✅ Compressão GZIP ativa
- ✅ Rate limiting
- ✅ Cache de assets

**Sem Nginx:**
- ❌ Sem compressão
- ❌ Sem rate limiting
- ❌ Expõe porta 8080 diretamente

### Otimizações Futuras

1. **Load Balancing** (múltiplas instâncias da API)
2. **SSL Termination** no Nginx
3. **CDN** para assets estáticos
4. **Compression** mais agressiva
5. **Cache** de respostas da API

---

## 🔍 Verificações

### Checklist de Configuração

- [x] Nginx configurado e rodando
- [x] API acessível via Nginx
- [x] Logs funcionando
- [x] Rate limiting ativo
- [x] Forward headers configurados
- [x] Health check respondendo
- [ ] SSL/HTTPS configurado (produção)
- [ ] Monitoramento configurado (produção)

---

## 📚 Referências

- [Nginx Documentation](https://nginx.org/en/docs/)
- [Spring Boot Behind Proxy](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.web.advertise-server)
- [Docker Compose](https://docs.docker.com/compose/)

---

## 🆘 Suporte

Para dúvidas ou problemas:
1. Verificar logs: `docker-compose logs`
2. Testar health check: `curl http://localhost/health`
3. Verificar conectividade: `docker-compose ps`

