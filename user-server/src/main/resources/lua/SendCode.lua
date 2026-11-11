local key = KEYS[1]
local code = ARGV[1]
local expire = ARGV[2]

if redis.call('exists', key) == 1 then
    return 0;
end
redis.call('setex', key, expire, code)
return 1


