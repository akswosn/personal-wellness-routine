== WellFlow TFLite 모델 안내 ==

카카오톡 대화 감정 분류에 사용되는 온디바이스 TFLite 모델 파일을 이 디렉토리에 배치해야 합니다.

[ 필요 파일 ]
  klue_emotion.tflite  (~10MB)

[ 모델 준비 방법 ]

1. HuggingFace에서 KLUE-BERT 기반 한국어 감정 분류 모델 다운로드
   - 추천 모델: snunlp/KR-FinBert-SC  또는  klue/roberta-base

2. optimum CLI로 TFLite 변환 (FP16 양자화):
   pip install optimum[exporters]
   optimum-cli export tflite \
     --model klue/roberta-base \
     --task text-classification \
     --int8 \
     --output klue_emotion_tflite/

3. 변환된 model.tflite 파일을 이 디렉토리에 복사:
   cp klue_emotion_tflite/model.tflite app/src/main/assets/klue_emotion.tflite

[ 성능 기준 (Galaxy S25 Ultra) ]
  - 메시지당 처리 속도: ~5ms
  - 1,000개 메시지 기준: 약 5초
  - 분류 클래스: 긍정 / 중립 / 부정 (3-class)

[ 모델 파일 없을 때 ]
  KakaoViewModel에서 모델 파일 존재 여부를 체크하고,
  없을 경우 키워드 기반 fallback 분석으로 자동 전환됩니다.

[ 참고 ]
  TFLite 파일은 용량(~10MB)으로 인해 Git에 포함하지 않습니다.
  베타 빌드(v0.0.1)에서는 키워드 기반 감정 분석으로 동작합니다.
