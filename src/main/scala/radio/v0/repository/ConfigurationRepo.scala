package radio.v0
package repository

trait ConfigurationRepo[T] {
  def config: T
}
