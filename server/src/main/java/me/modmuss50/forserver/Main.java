package me.modmuss50.forserver;


import com.unrealdinnerbone.unreallib.json.JsonUtil;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Main  {

	private static final Logger SWITCH = LoggerFactory.getLogger("Switch");
	private static final Logger STATION = LoggerFactory.getLogger("Station");
	private static final Logger COMPUTER = LoggerFactory.getLogger("Computer");
	public static Javalin APP = Javalin.create().start(9999);
	public static DataManager dataManager;

	static {
		try {
			dataManager = DataManager.read();
		} catch (Exception e) {
			throw new RuntimeException("Failed to load data");
		}
	}

	public static void main(String[] args) {

		APP.get("/", ctx -> ctx.result("Hello"));



		//Todo add checking
		jsonPost(Types.Station.class, Types.DefaultResponse.class, "/station/new", station -> {
			STATION.info("New Station Added: {}", station.name);
			dataManager.stations.put(station.id, station);
			dataManager.save();
			return null;
		});

		//Todo add checking
		jsonPost(Types.Switch.class, Types.DefaultResponse.class, "/switch/new", sw -> {
			SWITCH.info("New Switch Added: {}", sw.name);
			dataManager.switches.put(sw.id, sw);
			dataManager.save();
			return null;
		});

		jsonPost(Types.SwitchRequest.class, Types.SwitchResponse.class, "/switch/request", sw -> {
			SWITCH.info("Train requesting switching into at {}", sw.info.name);
			final boolean[] shouldSwitch = { false };

			Utils.ifValid(sw.minecart.dest, str -> {
				SWITCH.info("Train traveling to: " + str);
				Types.ComputerData dest = dataManager.getByName(str);
				Types.ComputerData current = dataManager.getByID(sw.info.id);
				Pathfinder pathfinder = new Pathfinder();
				pathfinder.build(dataManager);
				try {
					Types.ComputerData next = pathfinder.getNext(current, dest);
					SWITCH.info("Next point: {}", next.name);
					Types.Switch aSwitch = (Types.Switch) current;
					SWITCH.info("Turn to {}", aSwitch.turnsTo);
					if(aSwitch.turnsTo.equalsIgnoreCase(next.name)){
						SWITCH.info("Switching train onto other line");
						shouldSwitch[0] = true;
					}
				}catch(RuntimeException e) {
					SWITCH.info("Error", e);
				}

			});

			Types.SwitchResponse response = new Types.SwitchResponse();
			response.shouldSwitch = shouldSwitch[0];
			return response;
		});

		jsonPost(Types.ListRequest.class, Types.ComputerList.class, "/computer/list", request -> {
			Types.ComputerList list = new Types.ComputerList();
			list.computers = dataManager.getAll().stream()
				.filter(computerData -> {
					if(request.ingoreId != null && request.ingoreId.equalsIgnoreCase(computerData.id)){
						return false;
					}
					if(request.type == null || request.type.isEmpty()){
						return true;
					}
					if(request.type.equalsIgnoreCase("station")){
						return computerData instanceof Types.Station;
					}
					if(request.type.equalsIgnoreCase("switch")){
						return computerData instanceof Types.Station;
					}
					return false;
				}).collect(Collectors.toList());
			return list;
		});
	}

	public static <T, R extends Types.DefaultResponse> void jsonPost(Class<T> type, Class<R> rClass, String route, Function<T, R> function){
		APP.post(route, ctx -> {
			T request = JsonUtil.DEFAULT.parse(type, ctx.body());
			R object = function.apply(request);
			if(object == null) {
				ctx.result(JsonUtil.DEFAULT.toJson(Types.DefaultResponse.class, new Types.DefaultResponse()));
			}else {
				object.status = "success";
				String response = JsonUtil.DEFAULT.toJson(rClass, object);
				ctx.result(response);
			}
		});
	}

	public static <T extends Types.DefaultResponse> void jsonGet(String route, Supplier<T> supplier){
		APP.get(route, ctx -> {
			Types.DefaultResponse object = supplier.get();
			if(object == null) object = new Types.DefaultResponse();
			object.status = "success";
			String response = JsonUtil.DEFAULT.toJson(Types.DefaultResponse.class, object);
			ctx.result(response);
		});
	}
}
