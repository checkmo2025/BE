local time = redis.call('TIME')
local now = time[1] * 1000 + math.floor(time[2] / 1000)
local day = math.floor((time[1] + 32400) / 86400)
local state = redis.call('HMGET', KEYS[1], 'day', 'count', 'next', 'blocked')
local count = tonumber(state[2]) or 0
if tonumber(state[1]) ~= day then count = 0 end
if count >= tonumber(ARGV[2]) then return -1 end
local wait = math.max(tonumber(state[3]) or 0, tonumber(state[4]) or 0) - now
if wait > 0 then return wait end
redis.call('HSET', KEYS[1], 'day', day, 'count', count + 1, 'next', now + tonumber(ARGV[1]))
redis.call('PEXPIRE', KEYS[1], 172800000)
return 0
