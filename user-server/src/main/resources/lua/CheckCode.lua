local key = KEYS[1]
local code = ARGV[1]

local rawCode = redis.call('get', key)

if rawCode == nil then
    return -1
elseif rawCode == code then
    redis.call('del', key)
    return 1
else
    return 0
end