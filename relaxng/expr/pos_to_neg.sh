cat pos.rnc > neg.rnc
sed -i -e 's/pos\./neg\./g' neg.rnc
sed -i -e 's/positive/negative/g' neg.rnc
sed -i -e 's/Positive/Negative/g' neg.rnc
sed -i -e 's/eqOp | equalOp/neOp | notEqualOp/g' neg.rnc
