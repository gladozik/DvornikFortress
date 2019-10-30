package game.map

import game.actors.Dwarf
import game.objects.Block
import game.objects.BlockType
import next
import kotlin.random.Random

class Map(val height: Int = 256, val width: Int = 256, depth: Int = 64, dwarfCount: Int = 1) {
    private val serializer = MapSerializer()

    private val map: Array<Array<Array<Block>>>

    init {
        map = Array(height) { x ->  Array(width) { y -> Array(depth) { z-> MapGenerator.generateBlock(x, y, z) }}}
        MapGenerator.addDwarfs(map, dwarfCount)
    }

    var x = posX
    var y = posY
    var z = posZ

    private fun getVisibleWidthBounds(): Pair<Int, Int> {
        return Pair(x - Cursor.width, x + Cursor.width)
    }

    private fun getVisibleHeightBounds(): Pair<Int, Int> {
        return Pair(y - Cursor.height, y + Cursor.height)
    }

    fun isOnCursor(x: Int, y: Int): Boolean {
        return this.x == x && this.y == y
    }

    private fun isAir(x: Int, y: Int, z: Int): Boolean {
        return map[x][y][z].blockType == BlockType.NONE
    }

    private fun isEmpty(x: Int, y: Int, z: Int): Boolean {
        return map[x][y][z].occupant == null
    }

    /**
     * If block at the coordinates is empty, returns block below.
     */
    fun getBlock(x: Int, y: Int, z: Int): Block {
        return if (isAir(x, y, z) && z > 0) map[x][y][z - 1] else map[x][y][z]
    }

    fun getHoverInfo(x: Int, y: Int, z: Int): String {
        return if (!isEmpty(x, y, z)) //has dwarfs
            map[x][y][z].occupant.toString()
        else
            getBlock(x, y, z).blockType.locale
    }

    override fun toString(): String {
        val widthBounds = getVisibleWidthBounds()
        val heightBounds = getVisibleHeightBounds()
        for (y in heightBounds.first..heightBounds.second) {
            for (x in widthBounds.first..widthBounds.second) {
                if (x < 0 || y < 0 || x >= width || y >= height) {
                    //for positions outside map, draw empty space
                    serializer.appendEmpty()
                } else {
                    serializer.appendBlock(x, y, z)
                }
            }
            serializer.appendHelp(y - heightBounds.first).appendNewLine()
        }
        return serializer.appendCursorInfo(x, y, z).finalize()
    }

    companion object Cursor {
        const val DIRT_HEIGHT = 48

        //current position of the screen center
        var posX = 128
        var posY = 128
        var posZ = DIRT_HEIGHT + 1

        //we draw $width$ blocks to the left of the cursor, and same amount to the right
        var width = 30
        var height = 12
    }

    object MapGenerator {
        private val random = Random

        fun generateBlock(x: Int, y: Int, z: Int): Block {
            return when {
                z > DIRT_HEIGHT + 1 -> Block(BlockType.NONE)
                z == DIRT_HEIGHT + 1 -> generateSurface()
                z == DIRT_HEIGHT -> Block(BlockType.DIRT)
                z == 0 -> Block(BlockType.BEDROCK)
                else -> generateUnderground(z)
            }
        }

        private fun generateSurface(): Block {
            return if (random.nextFloat() < 0.05) Block(BlockType.WOOD) else Block(BlockType.NONE)
        }

        private fun generateUnderground(z: Int): Block {
            val rnd = random.nextFloat()
            return Block(when {
                rnd < scaleWithHeight(0.02, z) -> BlockType.GOLD
                rnd < scaleWithHeight(0.05, z) -> BlockType.IRON
                else -> BlockType.STONE
            })
        }

        /**
         * At DIRT_HEIGHT base would be halved, at 0 - doubled.
         */
        private fun scaleWithHeight(base: Double, z: Int): Double {
            return base * 2 * (1 - z / (DIRT_HEIGHT * 1.25))
        }

        /**
         * Adds dwarfs to the generated map.
         * Starts at cursor location and goes around in spiral, trying to find empty places to put dwarfs in.
         *   ┍━━┑
         *   │┍┑│
         *  ││+││
         *  │┕━┙│
         *  ┕━━━┙
         *  (that's a spiral, not a paper clip)
         */
        fun addDwarfs(map: Array<Array<Array<Block>>>, dwarfCount: Int) {
            var dwarfsNeeded = dwarfCount
            var stepSize = 1
            var stepCounter = 0
            val MAX_STEPS = 2
            var direction = Direction.NORTH
            var x = posX
            var y = posY
            //while we need more dwarfs and we are still on the map
            while (dwarfsNeeded > 0 && map.indices.contains(x) && map[x].indices.contains(y)) {
                if (map[x][y][posZ].occupant == null && map[x][y][posZ].blockType == BlockType.NONE) {
                    map[x][y][posZ].occupant = Dwarf(x, y, posZ)
                    dwarfsNeeded--
                } else {
                    if (stepCounter == MAX_STEPS) {
                        stepSize += 1
                        stepCounter = 0
                        direction = direction.next()
                    }

                    when (direction) {
                        Direction.NORTH -> y -= 1
                        Direction.EAST -> x += 1
                        Direction.SOUTH -> y += 1
                        Direction.WEST -> x -= 1
                    }
                    stepCounter += 1
                }
            }
        }

        private enum class Direction {
            NORTH, EAST, SOUTH, WEST
        }
    }
}

