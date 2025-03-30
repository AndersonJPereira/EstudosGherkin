package utils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import classes.LoginData;
import classes.LoginData.User;

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

	public static User buscarUsuarioPorStatus(String status) {

		List<User> users = getLoginData().users;

		for (User user: users) {
			if (user.status.equals(status)) {
				return user;
			}
		}

		throw new RuntimeException("Usuário com status '" + status + "' não encontrado");
	}


}
