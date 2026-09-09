-- 전역 버전 행(id=1)을 미리 만든다.
--
-- 버전 발급은 이 행을 잠그고(SELECT ... FOR UPDATE) 진행하는데, 없는 행은 잠글 수 없다.
-- 행 생성을 발급 시점에 맡기면 최초 동시 요청에서 경합이 생기므로 스키마 단계에서 넣어둔다.
INSERT INTO catalog_version (id, version)
VALUES (1, 0)
ON CONFLICT (id) DO NOTHING;
