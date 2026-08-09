# Neo Music Backend — 데이터 모델 (확정본)

> MVP 기준. 델타 동기화(계획서 §3.5)를 1급 시민으로 설계.

## ERD (개념)

```
Song (1) ──< Track (N)      # 언어별 오디오
Song (1) ──< Lyric (N)      # 언어별 가사

CatalogVersion (single-row) # 전역 카탈로그 버전 카운터
```

---

## Song (곡) — 애그리거트 루트
| 필드 | 타입 | 제약/기본 | 설명 |
|---|---|---|---|
| `id` | String | PK, 유니크, not blank | 읽기 쉬운 곡 코드 (예: `song_001`) |
| `title` | String | not blank | 제목 |
| `artist` | String | not blank | 아티스트 |
| `coverKey` | String? | nullable | 커버 이미지 **저장 키**(URL 아님) |
| `displayOrder` | Int | default 0 | 큐레이션 순서 |
| `lastModifiedVersion` | Long | not null, 인덱스 | **델타 동기화용** 전역 버전 스탬프 |
| `isDeleted` | Boolean | default false | tombstone(삭제 전파용) |
| `createdAt` | Instant | not null | 생성 시각 |
| `updatedAt` | Instant | not null | 수정 시각 |

## Track (언어별 오디오) — Song 하위
| 필드 | 타입 | 제약 | 설명 |
|---|---|---|---|
| `id` | Long | PK, auto | 내부 식별자 |
| `songId` | String | FK→Song, (songId,lang) 유니크 | 소속 곡 |
| `lang` | String | not blank | `ko`, `en` … |
| `label` | String | not blank | `한국어`, `English` |
| `audioKey` | String | not blank | 오디오 파일 **저장 키** |
| `durationMs` | Int | ≥0 | 재생 길이 |
| `bytes` | Long | ≥0 | 파일 크기 |
| `checksum` | String | not blank | sha256 — **파일 재다운로드 스킵 판단** |

## Lyric (언어별 가사) — Song 하위
| 필드 | 타입 | 제약 | 설명 |
|---|---|---|---|
| `id` | Long | PK, auto | 내부 식별자 |
| `songId` | String | FK→Song, (songId,lang) 유니크 | 소속 곡 |
| `lang` | String | not blank | 언어 |
| `type` | enum | `IMAGE` / `TEXT` | 1차 IMAGE, 2차 TEXT |
| `imageKeys` | List\<String> | 순서 보존 | 가사 이미지 저장 키(여러 페이지) |
| *(lines)* | — | Phase 2 | 텍스트 가사 줄+타임스탬프(+코드) 확장 자리 |

## CatalogVersion (전역 버전)
| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | Int | 항상 1 (단일 행) |
| `version` | Long | 현재 전역 카탈로그 버전 |

**동작**: 곡을 생성/수정/삭제할 때마다 `version += 1` → 그 값을 해당 곡의 `lastModifiedVersion`에 기록.

---

## 델타 동기화 계약 (핵심 API)

```
GET /catalog?since={version}
```
- `since` 없음(첫 실행) → 삭제 안 된 전체 곡, `version` = 현재 전역 버전
- `since = X` → `lastModifiedVersion > X` 인 곡만
  - `changed`: 그중 `isDeleted=false` 인 곡(트랙·가사 포함 전체)
  - `deleted`: 그중 `isDeleted=true` 인 곡의 id 목록

응답 예시:
```jsonc
{
  "version": 42,
  "changed": [ { "id": "song_001", "title": "...", "tracks": [...], "lyrics": [...] } ],
  "deleted": ["song_007"]
}
```

## 저장 키 → URL 변환
- 엔티티는 `coverKey` / `audioKey` / `imageKeys` = **저장 키(경로)** 만 보관
- 응답 DTO로 변환할 때 **StoragePort가 signed URL 생성** → 클라는 URL로 다운로드
- 저장소(로컬→S3) 교체해도 도메인·DB 불변

## 확정 결정 로그
1. URL 대신 **저장 키** 저장 + API에서 signed URL 생성
2. `rev` **제거** (lastModifiedVersion + checksum으로 충분, 중복 상태 제거)
3. Song ID = **읽기 쉬운 고유 문자열**(`song_001`)
