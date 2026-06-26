-- Move 'Architecture' topic (under Career) to 'Technology' category (id = 1)
UPDATE topics 
SET category_id = (SELECT id FROM categories WHERE name = 'Technology') 
WHERE title = 'Architecture';

-- Delete other topics belonging to categories 'Career', 'Finance', 'Health', 'General'
DELETE FROM topics 
WHERE category_id IN (
    SELECT id FROM categories 
    WHERE name IN ('Career', 'Finance', 'Health', 'General')
);

-- Delete the categories 'Career', 'Finance', 'Health', 'General'
DELETE FROM categories 
WHERE name IN ('Career', 'Finance', 'Health', 'General');
