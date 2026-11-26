# My Camera

Android 포토부스 앱

## 주요 기능

- **프레임 선택** · 3컷 세로형 · 4컷 그리드
- **자동 연속 촬영** · 컷당 5초 카운트다운 · 촬영 성공 플래시
- **슬롯 재촬영** · 게스트당 1회 · 선택한 슬롯만 교체 · 실패·취소 시 원본과 횟수 유지
- **결과 저장** · 프레임 합성 JPEG · 갤러리 My Camera 앨범
- **다음 게스트** · 같은 프레임으로 새 촬영 · 재촬영 횟수 초기화
- **첫 실행 튜토리얼** · 촬영·재촬영·저장 안내
- **화면 방향** · 세로·가로 모드 지원

## 사용 스택

- **언어·UI** · Kotlin · Jetpack Compose · Material 3 · Navigation Compose
- **상태 관리** · ViewModel · MVI · Kotlin Coroutines · StateFlow
- **의존성 주입** · Hilt
- **카메라** · CameraX
- **이미지 합성·저장** · Bitmap · Canvas · MediaStore
- **설정 저장** · Preferences DataStore
