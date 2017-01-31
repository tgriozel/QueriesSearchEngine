package queriessearchengine

import scala.math.Ordered.orderingToOrdered

private sealed trait BTree[+T]
private object EmptyTree extends BTree[Nothing]
private case class BNode[T](value: T , left: BTree[T] = EmptyTree, right: BTree[T] = EmptyTree) extends BTree[T]

private class OrderedIntervalsTree[T: Ordering] (comparableElements: Array[T]) {
  private val tree = binaryTreeFromSortedArray(comparableElements.distinct.sorted)

  private def binaryTreeFromSortedArray(orderedArray: Array[T]): BTree[T] = {
    orderedArray.length match {
      case 0 => EmptyTree
      case 1 => BNode(orderedArray(0))
      case 2 => BNode(orderedArray(1), BNode(orderedArray(0)), EmptyTree)
      case 3 => BNode(orderedArray(1), BNode(orderedArray(0)), BNode(orderedArray(2)))
      case _ => {
        val mid = orderedArray.length / 2 + 1
        val leftTree = binaryTreeFromSortedArray(orderedArray.slice(0, mid))
        val rightTree = binaryTreeFromSortedArray(orderedArray.slice(mid + 1, orderedArray.length))
        BNode(orderedArray(mid), leftTree, rightTree)
      }
    }
  }

  private def nodeRangeLookup(node: BNode[T], target: T): (T, T) = {
    target.compare(node.value) match {
      case comp if comp < 0 => {
        node.left match {
          case EmptyTree => (node.value, node.value)
          case leftNode: BNode[T] => {
            target.compare(leftNode.value) match {
              case leftComp if leftComp > 0 => (leftNode.value, node.value)
              case _ => nodeRangeLookup(leftNode, target)
            }
          }
        }
      }
      case comp if comp > 0 => {
        node.right match {
          case EmptyTree => (node.value, node.value)
          case rightNode: BNode[T] => {
            target.compare(rightNode.value) match {
              case rightComp if rightComp < 0 => (node.value, rightNode.value)
              case _ => nodeRangeLookup(rightNode, target)
            }
          }
        }
      }
      case _ => {
        (node.value, node.value)
      }
    }
  }

  def correspondingInterval(target: T): (T, T) = {
    nodeRangeLookup(tree.asInstanceOf[BNode[T]], target)
  }
}
