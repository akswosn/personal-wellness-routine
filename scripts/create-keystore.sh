#!/bin/bash
# ============================================================
#  WellFlow 릴리즈 Keystore 생성 스크립트
#  실행: bash scripts/create-keystore.sh
# ============================================================

set -e
cd "$(dirname "$0")/.."  # 프로젝트 루트로 이동

OUTPUT="wellflow-release.jks"

if [ -f "$OUTPUT" ]; then
    echo "⚠️  $OUTPUT 이미 존재합니다. 덮어쓰려면 먼저 삭제하세요."
    exit 1
fi

echo "=========================================="
echo "  WellFlow 릴리즈 Keystore 생성"
echo "=========================================="
echo ""
echo "비밀번호는 keystore.properties 에 동일하게 입력해야 합니다."
echo ""

read -s -p "🔑 Store 비밀번호 (8자 이상): " STORE_PASS && echo
read -s -p "🔑 Store 비밀번호 확인:        " STORE_PASS2 && echo

if [ "$STORE_PASS" != "$STORE_PASS2" ]; then
    echo "❌ 비밀번호가 일치하지 않습니다."
    exit 1
fi

read -s -p "🔑 Key 비밀번호 (8자 이상):   " KEY_PASS && echo
read -s -p "🔑 Key 비밀번호 확인:         " KEY_PASS2 && echo

if [ "$KEY_PASS" != "$KEY_PASS2" ]; then
    echo "❌ 비밀번호가 일치하지 않습니다."
    exit 1
fi

echo ""
echo "🔧 Keystore 생성 중..."

keytool -genkey -v \
    -keystore "$OUTPUT" \
    -alias wellflow \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -storepass "$STORE_PASS" \
    -keypass "$KEY_PASS" \
    -dname "CN=WellFlow, OU=App, O=forlks, L=Seoul, S=Seoul, C=KR"

echo ""
echo "✅ $OUTPUT 생성 완료!"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  다음 단계:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "1️⃣  SHA-1 확인 (Firebase/GCP 등록용)"
echo ""
keytool -list -v \
    -keystore "$OUTPUT" \
    -alias wellflow \
    -storepass "$STORE_PASS" \
    2>/dev/null | grep -E "SHA1|SHA-1"

echo ""
echo "2️⃣  keystore.properties.template → keystore.properties 복사 후 값 입력:"
echo "    cp keystore.properties.template keystore.properties"
echo "    storePassword=$STORE_PASS"
echo "    keyPassword=$KEY_PASS"
echo ""
echo "3️⃣  SHA-1을 Firebase Console + GCP Console 에 등록"
echo ""
echo "⚠️  wellflow-release.jks 를 안전한 곳에 별도 백업하세요!"
echo "    절대 git 에 커밋하지 마세요 (.gitignore 에 포함됨)"
