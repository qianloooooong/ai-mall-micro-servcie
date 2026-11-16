local count = redis.call("incr", KEYS[1])

if count == 1 then
    redis.call("expire", KEYS[1], ARGV[1])
end

return count