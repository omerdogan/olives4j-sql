--@NAMED search_customer
select * 
from customer 
where 1=1 
and store_id = 1			--@:store_id optional
and first_name like 'A%'	--@:firstname optional
and active 	= true			--@:active optional
and create_date between (sysdate - 1) --@:start_date
				and		(sysdate + 1) --@:end_date
;

--@NAMED find_customer_by_id
select * 
from customer 
where 1=1 
and customer_id=:customer_id
;
