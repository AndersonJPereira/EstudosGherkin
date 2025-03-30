package utils;

import java.io.IOException;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.ObjectMapper;

import classes.LoginData;

public class JsonUtils {
	
	static String jsonLoginPath = ConfigReader.getProperty("json.login.path");

	public static LoginData getLoginData() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			return objectMapper.readValue(Paths.get(jsonLoginPath).toFile(),LoginData.class);
		} catch (IOException e) {
			 throw new RuntimeException("Erro ao ler JSON de login", e);
		}
	}
	
}
