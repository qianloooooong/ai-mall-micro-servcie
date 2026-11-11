local prefix = KEYS[1]
local limitPrefix = KEYS[2]
local key = KEYS[3]
local code = ARGV[1]
local codeExpire = ARGV[2]
local limiterExpire = ARGV[3]

if redis.call('exists', limitPrefix .. key) == 1 then
    return 0
end
redis.call('setex', prefix .. key, codeExpire, code)
redis.call('setex', limitPrefix .. key, limiterExpire, nil)
return 1


