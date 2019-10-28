
import kotlin.math.*
import kotlin.random.Random
import kotlin.system.measureTimeMillis

interface tree<T> {
	fun insert(x: T)
	fun contains(x: T): Boolean
}


open class avl<T: Comparable<T>>(var label: T, var left: avl<T>? = null, var right: avl<T>? = null) : tree<T> {

	var size = 0
	var height = 0
	init {
		left?.let { 
			if (it.label >= label) {
				throw IllegalArgumentException("left label >= label")
			}
		}
		right?.let { 
			if (it.label <= label) {
				throw IllegalArgumentException("right label <= label")
			}
		}
		setSize()
		setHeight()
	}
	
	fun setSize() {
		size = 1 
		size += if (left == null) 0 else left!!.size
		size += if (right == null) 0 else right!!.size
	}

	fun setHeight() {
		height = 1
		val a = if (left == null) 0 else left!!.height
		val b = if (right == null) 0 else right!!.height
		height += max(a, b)
	}

	fun lean(): Int { 
		val a = if (left == null) 0 else left!!.height
		val b = if (right == null) 0 else right!!.height
		return b - a
	}

	fun balance() {
		if (lean() == -2) {
			left?.let {
				when (it.lean()) {
					-1 -> rr()
					1 -> lr()
					else -> println("don't know how to balance...")
				}
			}
		} else if (lean() == 2) {
			right?.let {
				when (it.lean()) {
					-1 -> rl()
					1 -> ll()
					else -> println("don't know how to balance...(2)")
				}
			}
		}
	}

	fun ll() {
		left = avl(label, left, right!!.left)
		label = right!!.label
		right = right!!.right
		setHeight()
	}

	fun rr() {
		right = avl(label, left!!.right, right)
		label = left!!.label
		left = left!!.left
		setHeight()
	}

	fun lr() {
		left!!.ll()
		rr()
	}

	fun rl() {
		right!!.rr()
		ll()
	}

	fun inorder(): Sequence<T> = sequence {
		left?.apply { 
			yieldAll(inorder()) 
		}
		yield(label)
		right?.apply { 
			yieldAll(inorder()) 
		}
	}.take(size)
	
	override fun insert(x: T) {
		val diff = x.compareTo(label)
		if (diff == 0) {
			throw IllegalArgumentException("value already exists")
		} else if (diff < 0) {
			if (left == null) {
				left = avl(x)
			} else {
				left!!.insert(x)
			}
		} else {
			if (right == null) {
				right = avl(x)
			} else {
				right!!.insert(x)
			}
		}
		size += 1
		setHeight()
		balance()
	}

	override fun contains(x: T): Boolean {
		var a = label == x
		left?.let {
			a = a || it.contains(x)
		}
		right?.let {
			a = a || it.contains(x)
		}
		return a
	}
}

fun <T> randomizeList(l: MutableList<T>): MutableList<T> {
	val rand = Random.Default
	for (i in 0..(l.size-1)) {
		val swap = rand.nextInt(i, l.size)
		val tmp = l[swap]
		l[swap] = l[i]
		l[i] = tmp
	}
	return l
}

fun test(testSize: Int) {
	val maxExpectedHeight = 1.44 * log(testSize.toDouble(), 2.toDouble())
	val testList = (1..testSize).toMutableList()	
	val randomList = randomizeList(testList)
	
	val tree = avl<Int>(randomList.first())
	randomList.drop(1).forEach {
		tree.insert(it)
	}

	var max = 0
	tree.inorder().forEach {
		if (it <= max) {
			throw IllegalStateException("tree is not sorted at value $it")
		}
		max = it
	}
	println("tree is sorted !")

	println("test tree size ${tree.size}")
	println("max expected height $maxExpectedHeight")
	println("test tree height ${tree.height}")
	println("test tree lean ${tree.lean()}")
}

fun main(args: Array<String>) {	
	val testSize = 1000000
	val timeTook = measureTimeMillis {
		test(testSize)
	}
	println("done: total time: $timeTook millis for tree with $testSize nodes")
}

