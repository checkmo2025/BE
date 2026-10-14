if redis.call('GET', KEYS[1]) ~= ARGV[1] then return 0 end
redis.call('MSET', KEYS[2], ARGV[2], KEYS[3], ARGV[3])
return 1
