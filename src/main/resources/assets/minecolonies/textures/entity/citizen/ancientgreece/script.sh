for i in *_a.png ; do
    cp $i ${i%_a.png}_d.png
done
