package messager.common;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Directory {
	private static final Logger LOGGER = LogManager.getLogger(Directory.class);

	/**
	 * 중간 디렉터리가 존재하지 않아도 생성
	 * 
	 * @param fullPath 디렉터리 전체 경로 
	 */
	public static void createDirectories(String fullPath) {
		try {
			Path path = null;
			path = Paths.get(fullPath);

			// 디렉터리가 존재하지 않으면 생성합니다.
			if (!Files.exists(path)) {
				Files.createDirectories(path);
				LOGGER.info("디렉터리 생성 성공: {}", path);
			}
		} catch (Exception e) {
			LOGGER.error("디렉터리 생성 실패: ", e);
		}
	}
}
