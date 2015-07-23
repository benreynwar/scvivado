library ieee;
use ieee.std_logic_1164;

entity Dummy is
  port (
    i: std_logic,
    o: std_logic,
    );
end Dummy;

architecture arch of Dummy is
begin
  o <= i;
end arch;
