package radio.v0
package repository

trait AppConfig[C, R, F] {
  def config: C
  def contentRepo: R
  def feedRepo: F
}

