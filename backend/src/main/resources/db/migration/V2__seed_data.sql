-- Seed categories
INSERT INTO categories (name, description) VALUES
('Technology', 'Software engineering, infrastructure, and technical skills'),
('Career', 'Professional development and leadership'),
('Finance', 'Investing and economics'),
('Health', 'Nutrition and fitness'),
('General', 'History, psychology, philosophy, and science');

-- Seed sub-categories as topics under Technology (we use topic titles to represent sub-areas)
-- Default user
INSERT INTO users (name, email) VALUES ('Demo User', 'demo@knowledgeos.dev');

-- Java topics (category_id = 1 Technology)
INSERT INTO topics (category_id, title, description, difficulty, estimated_minutes, content) VALUES
(1, 'Java Collections', 'Master the Java Collections Framework including List, Set, Map, and Queue implementations.',
 'BEGINNER', 45,
 'The Java Collections Framework provides a unified architecture for representing and manipulating collections. Key interfaces include Collection, List, Set, Queue, and Map. ArrayList offers O(1) random access, LinkedList excels at insertions, HashMap provides O(1) average lookup, and TreeMap maintains sorted order. Choose implementations based on access patterns and ordering requirements.'),

(1, 'Java Streams', 'Learn functional-style data processing with the Stream API introduced in Java 8.',
 'INTERMEDIATE', 60,
 'Streams enable declarative data processing pipelines. Key operations include map, filter, reduce, flatMap, collect, and groupingBy. Streams are lazy, can be parallelized with parallelStream(), and work with primitive specializations (IntStream, LongStream, DoubleStream). Terminal operations trigger evaluation.'),

(1, 'Java Generics', 'Understand type parameters, wildcards, bounded types, and type erasure.',
 'INTERMEDIATE', 50,
 'Generics provide compile-time type safety. Use <T> for type parameters, ? extends T for upper bounds (producer), ? super T for lower bounds (consumer) following PECS principle. Type erasure means generic type information is removed at runtime.'),

(1, 'Java Concurrency', 'Fundamentals of multi-threading, synchronization, and thread safety.',
 'ADVANCED', 90,
 'Java concurrency covers threads, synchronized blocks, volatile variables, atomic classes, and the java.util.concurrent package. Understand happens-before relationships, visibility, and atomicity. Prefer higher-level constructs over raw threads.'),

(1, 'ExecutorService', 'Thread pool management and task execution frameworks.',
 'ADVANCED', 60,
 'ExecutorService manages thread pools for efficient task execution. Use Executors.newFixedThreadPool(), newCachedThreadPool(), or newSingleThreadExecutor(). Submit Callable tasks for results, use invokeAll for batch processing, and always shutdown() gracefully.'),

(1, 'CompletableFuture', 'Asynchronous programming with composable futures.',
 'ADVANCED', 75,
 'CompletableFuture enables non-blocking async composition. Chain operations with thenApply, thenCompose, thenCombine. Handle errors with exceptionally or handle. Use allOf/anyOf for coordination. Integrates with ExecutorService for custom thread pools.'),

(1, 'Virtual Threads', 'Project Loom lightweight threads for high-throughput applications.',
 'ADVANCED', 45,
 'Virtual threads (JEP 444) are lightweight threads managed by the JVM. Create with Thread.startVirtualThread() or Executors.newVirtualThreadPerTaskExecutor(). Ideal for I/O-bound workloads. Avoid pinning by not synchronizing on native monitors during blocking I/O.');

-- Spring topics
INSERT INTO topics (category_id, title, description, difficulty, estimated_minutes, content) VALUES
(1, 'Bean Lifecycle', 'Understanding Spring bean creation, initialization, and destruction.',
 'INTERMEDIATE', 40,
 'Spring beans go through instantiation, dependency injection, @PostConstruct initialization, use, and @PreDestroy cleanup. Bean scopes include singleton (default), prototype, request, session. BeanFactoryPostProcessors and BeanPostProcessors customize the lifecycle.'),

(1, 'Spring Boot', 'Auto-configuration, starters, and production-ready features.',
 'INTERMEDIATE', 60,
 'Spring Boot simplifies Spring development with auto-configuration, starter dependencies, embedded servers, and Actuator. Use @SpringBootApplication, application.properties/yml, profiles, and @Conditional annotations. The spring.factories mechanism drives auto-config.'),

(1, 'Spring JPA', 'Object-relational mapping with Spring Data JPA and Hibernate.',
 'INTERMEDIATE', 75,
 'Spring Data JPA provides repository abstractions over JPA/Hibernate. Define entities with @Entity, relationships with @OneToMany/@ManyToOne, and repositories extending JpaRepository. Use @Query for custom queries, pagination with Pageable, and projections for DTOs.'),

(1, 'Spring Security', 'Authentication, authorization, and security filter chains.',
 'ADVANCED', 90,
 'Spring Security uses a filter chain for request processing. Configure with SecurityFilterChain bean, enable method security with @PreAuthorize. Support form login, JWT, OAuth2. Understand AuthenticationManager, UserDetailsService, and PasswordEncoder.');

-- Kafka topics
INSERT INTO topics (category_id, title, description, difficulty, estimated_minutes, content) VALUES
(1, 'Kafka Partitions', 'Partitioning strategies for scalability and ordering guarantees.',
 'INTERMEDIATE', 45,
 'Partitions enable Kafka parallelism. Messages within a partition are ordered. Partition key determines target partition via hash. More partitions increase throughput but also overhead. Consider partition count based on consumer parallelism and retention.'),

(1, 'Consumer Groups', 'Cooperative consumption and load balancing across consumers.',
 'INTERMEDIATE', 50,
 'Consumer groups distribute partition consumption. Each partition is consumed by one consumer in a group. Add consumers to scale (up to partition count). Group coordinator manages membership. Offsets track consumption progress per partition.'),

(1, 'Kafka Rebalancing', 'Partition reassignment when group membership changes.',
 'ADVANCED', 55,
 'Rebalancing redistributes partitions when consumers join/leave. Strategies include Range, RoundRobin, Sticky, and Cooperative Sticky. Rebalancing causes stop-the-world pauses. Minimize with static membership and cooperative protocols.'),

(1, 'Kafka Transactions', 'Exactly-once semantics with transactional producers and consumers.',
 'ADVANCED', 70,
 'Kafka transactions provide atomic multi-partition writes. Use transactional.id, beginTransaction(), commitTransaction(). Read_committed isolation for consumers. Idempotent producers prevent duplicates. Understand transaction coordinator role.');

-- System Design topics
INSERT INTO topics (category_id, title, description, difficulty, estimated_minutes, content) VALUES
(1, 'Caching', 'Cache strategies, eviction policies, and distributed caching.',
 'INTERMEDIATE', 60,
 'Caching reduces latency and database load. Patterns: cache-aside, read-through, write-through, write-behind. Eviction: LRU, LFU, TTL. Distributed caches: Redis, Memcached. Watch for cache stampede, invalidation complexity, and consistency.'),

(1, 'Load Balancing', 'Distributing traffic across servers for availability and scale.',
 'INTERMEDIATE', 50,
 'Load balancers distribute requests using round-robin, least connections, weighted, or consistent hashing. Layers: L4 (transport) vs L7 (application). Health checks ensure traffic routes to healthy instances. Examples: NGINX, HAProxy, AWS ALB.'),

(1, 'API Gateway', 'Centralized entry point for microservices APIs.',
 'INTERMEDIATE', 55,
 'API Gateways handle routing, authentication, rate limiting, SSL termination, and request transformation. Examples: Kong, AWS API Gateway, Spring Cloud Gateway. Benefits: single entry point, cross-cutting concerns. Risks: bottleneck, added latency.'),

(1, 'CQRS', 'Command Query Responsibility Segregation pattern.',
 'ADVANCED', 80,
 'CQRS separates read and write models. Commands modify state, queries read optimized views. Enables independent scaling and schema optimization. Often paired with Event Sourcing. Trade-offs: complexity, eventual consistency, operational overhead.');

-- Career topics
INSERT INTO topics (category_id, title, description, difficulty, estimated_minutes, content) VALUES
(2, 'Leadership', 'Leading teams effectively and developing leadership skills.',
 'INTERMEDIATE', 60, 'Leadership involves vision, communication, delegation, and empathy. Focus on servant leadership, 1:1s, feedback culture, and psychological safety.'),
(2, 'Communication', 'Effective technical and interpersonal communication.',
 'BEGINNER', 45, 'Clear communication is essential. Practice active listening, structured writing, presentation skills, and adapting message to audience.'),
(2, 'Architecture', 'Software architecture principles and decision-making.',
 'ADVANCED', 90, 'Architecture balances quality attributes: scalability, maintainability, security. Use ADRs, evaluate trade-offs, and align with business goals.');

-- Finance topics
INSERT INTO topics (category_id, title, description, difficulty, estimated_minutes, content) VALUES
(3, 'Investing', 'Fundamentals of personal investing and portfolio management.',
 'BEGINNER', 60, 'Understand asset classes, diversification, index funds, compound interest, and risk tolerance. Long-term perspective beats market timing.'),
(3, 'Economics', 'Micro and macroeconomic principles.',
 'INTERMEDIATE', 75, 'Supply and demand, inflation, monetary policy, GDP, and market structures form the foundation of economic thinking.');

-- Health topics
INSERT INTO topics (category_id, title, description, difficulty, estimated_minutes, content) VALUES
(4, 'Nutrition', 'Evidence-based nutrition fundamentals.',
 'BEGINNER', 45, 'Macronutrients, micronutrients, caloric balance, and whole foods. Avoid fad diets; focus on sustainable habits.'),
(4, 'Fitness', 'Exercise science and training principles.',
 'BEGINNER', 50, 'Progressive overload, cardiovascular health, strength training, recovery, and consistency over intensity.');

-- General topics
INSERT INTO topics (category_id, title, description, difficulty, estimated_minutes, content) VALUES
(5, 'History', 'Key historical events and their modern relevance.',
 'BEGINNER', 60, 'Understanding history provides context for current events and human behavior patterns.'),
(5, 'Psychology', 'Cognitive biases and human behavior.',
 'INTERMEDIATE', 55, 'Learn about confirmation bias, anchoring, loss aversion, and how they affect decisions.'),
(5, 'Philosophy', 'Introduction to philosophical thinking.',
 'INTERMEDIATE', 60, 'Explore ethics, epistemology, and major philosophical traditions.'),
(5, 'Science', 'Scientific method and key discoveries.',
 'BEGINNER', 50, 'Hypothesis testing, peer review, and interdisciplinary connections.');

-- Technology sub-area topics
INSERT INTO topics (category_id, title, description, difficulty, estimated_minutes, content) VALUES
(1, 'Databases', 'Relational and NoSQL database fundamentals.',
 'INTERMEDIATE', 75, 'SQL, normalization, indexing, transactions, and when to choose document, key-value, or graph databases.'),
(1, 'Kubernetes', 'Container orchestration with Kubernetes.',
 'ADVANCED', 90, 'Pods, Services, Deployments, ConfigMaps, and cluster management fundamentals.'),
(1, 'Cloud', 'Cloud computing concepts and major providers.',
 'INTERMEDIATE', 60, 'IaaS, PaaS, SaaS, AWS/GCP/Azure core services, and cloud-native patterns.'),
(1, 'AI Engineering', 'Building production AI systems.',
 'ADVANCED', 90, 'LLMs, RAG, fine-tuning, evaluation, and MLOps practices.'),
(1, 'React', 'Modern React development with hooks and ecosystem.',
 'INTERMEDIATE', 60, 'Components, hooks, state management, React Query, and performance optimization.'),
(1, 'DevOps', 'CI/CD, infrastructure as code, and operational excellence.',
 'INTERMEDIATE', 75, 'Pipelines, monitoring, logging, GitOps, and SRE principles.');

-- Prerequisites
INSERT INTO topic_prerequisites (topic_id, prerequisite_topic_id) VALUES
(2, 1),  -- Streams requires Collections
(3, 1),  -- Generics after Collections
(4, 1),  -- Concurrency after Collections
(5, 4),  -- ExecutorService after Concurrency
(6, 5),  -- CompletableFuture after ExecutorService
(7, 4),  -- Virtual Threads after Concurrency
(9, 8),  -- Spring Boot after Bean Lifecycle
(10, 9), -- JPA after Spring Boot
(11, 9), -- Security after Spring Boot
(13, 12), -- Consumer Groups after Partitions
(14, 13), -- Rebalancing after Consumer Groups
(15, 13), -- Transactions after Consumer Groups
(17, 16), -- Load Balancing after Caching
(18, 17), -- API Gateway after Load Balancing
(19, 16); -- CQRS after Caching

-- Learning paths
INSERT INTO learning_paths (name, description) VALUES
('Java Mastery', 'Complete path from collections to virtual threads'),
('Spring Developer', 'Spring ecosystem from beans to security'),
('Kafka Expert', 'Deep dive into Apache Kafka'),
('System Design Fundamentals', 'Core distributed systems patterns');

INSERT INTO learning_path_topics (learning_path_id, topic_id, sequence_no) VALUES
(1, 1, 1), (1, 2, 2), (1, 3, 3), (1, 4, 4), (1, 5, 5), (1, 6, 6), (1, 7, 7),
(2, 8, 1), (2, 9, 2), (2, 10, 3), (2, 11, 4),
(3, 12, 1), (3, 13, 2), (3, 14, 3), (3, 15, 4),
(4, 16, 1), (4, 17, 2), (4, 18, 3), (4, 19, 4);
