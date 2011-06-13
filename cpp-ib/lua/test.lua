
-- Example for OO programming
Accumulator = {}

function Accumulator.new()
   local self ={}
   self._value = 0

   function self.accumulate(value)
      self._value = self._value + value
      return self._value
   end

   function self.value()
      return self._value
   end

   return self
end


print('Accumulator test.')
acc = Accumulator.new()
print(acc.accumulate(7))
print(acc.accumulate(3))
print(acc.accumulate(32))
print(acc.value())

acc = Accumulator.new()
print(acc.accumulate(1))
print(acc.accumulate(1))
print(acc.value())

-- IB integration via Luabind:
print('IB contract')

c = Contract()
c.symbol = 'goog'
c.strike = 500
c.currency = 'USD'

-- Testing overloaded free functions
print('hello')
print(1234)
print(c)

