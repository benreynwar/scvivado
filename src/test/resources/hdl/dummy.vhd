library ieee;
use ieee.std_logic_1164.all;

entity Dummy is
  port (
    i: in std_logic;
    o: out std_logic
    );
end Dummy;

architecture arch of Dummy is
begin
  o <= i;
end arch;
