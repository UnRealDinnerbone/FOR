package me.modmuss50.forserver;

import com.unrealdinnerbone.unreallib.json.JsonUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

public class DataManager {

	public static Path data = Path.of("server_data").resolve(Path.of("for_data.json"));

	public HashMap<String, Types.Station> stations = new HashMap<>();
	public HashMap<String, Types.Switch> switches = new HashMap<>();

	public List<Types.ComputerData> getAll(){
		List<Types.ComputerData> list = new ArrayList<>();
		list.addAll(stations.values());
		list.addAll(switches.values());
		return list;
	}

	public Types.ComputerData getByID(String id){
		return getAll().stream().filter(computerData -> computerData.id.equals(id)).findFirst().orElseThrow(null);
	}

	public Types.ComputerData getByName(String name){
		return getAll().stream().filter(computerData -> computerData.name.equals(name)).findFirst().orElse(null);
	}

	public void save()  {
		try {
			Files.writeString(data, JsonUtil.DEFAULT.toJson(DataManager.class, this), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException("Failed to save", e);
		}
	}

	public static DataManager read() throws Exception {
		if(Files.exists(data)){
			String json = Files.readString(data, StandardCharsets.UTF_8);
			return JsonUtil.DEFAULT.parse(DataManager.class, json);
		} else {
			return new DataManager();
		}
	}

}
