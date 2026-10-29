local time = redis.call('TIME')
local now = time[1] * 1000 + math.floor(time[2] / 1000)
local untilAt = math.max(tonumber(redis.call('HGET', KEYS[1], 'blocked')) or 0, now + tonumber(ARGV[1]))
redis.call('HSET', KEYS[1], 'blocked', untilAt)
redis.call('PEXPIRE', KEYS[1], math.max(172800000, untilAt - now + 1000))
return 1
