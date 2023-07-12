local key = KEYS[1]
local req = tonumber(redis.call('GET', key) or '-1')
local max_req = tonumber(ARGV[1])
local exp_milli = tonumber(ARGV[2])

if (req == -1) or (req < max_req) then
  redis.call('INCR', key)
  redis.call('PEXPIRE', key, exp_milli)
  return false
else
  return true
end
