os.loadAPI("json.lua")
os.loadAPI("utils.lua")

local dataFile = "detector.json"

local function setup()
    print("Detector data not found, station setup tool:")
    local data = {
        pos = {
            x = tonumber(utils.readValue("x pos")),
            y = tonumber(utils.readValue("y pos")),
            z = tonumber(utils.readValue("z pos"))
        }
    }
    data.id = data.pos.x .. "," .. data.pos.y .. "," .. data.pos.z

    json.encodeToFile(dataFile, data)
end

local function dectectorMain()
    if not fs.exists(dataFile) then
        setup()
    end
    print("Reading " .. dataFile)
    local data = json.decodeFromFile(dataFile)

    while true do
        local cartName, destination = os.pullEvent("minecart")
        local passData = {
            info = data,
            minecart = {
                name = cartName,
                dest = destination
            }
        }
        print(json.encodePretty(passData))
        utils.httpPostAsync("detector/pass", passData)
    end
end

dectectorMain()